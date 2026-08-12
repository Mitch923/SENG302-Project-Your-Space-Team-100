package nz.ac.canterbury.seng302.homehelper.integration.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import jakarta.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import nz.ac.canterbury.seng302.homehelper.controller.CompetitionDetailsController;
import nz.ac.canterbury.seng302.homehelper.entity.Competition;
import nz.ac.canterbury.seng302.homehelper.entity.CompetitionDesign;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.CompetitionRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
@Transactional
public class CompetitionDetailsControllerIntegrationTest {

    @Autowired
    CompetitionDetailsController competitionDetailsController;
    @Autowired
    CompetitionRepository competitionRepository;
    @Autowired
    UserRepository userRepository;
    @SpyBean
    private UserService userService;
    private MockMvc mockMvc;
    private MvcResult mvcResult;
    private User user;


    @BeforeEach
    public void before() {
        mockMvc = MockMvcBuilders.standaloneSetup(competitionDetailsController).build();
        user = new User("jane@example.com", "P4$$word", "John", "Doe");
        user = userRepository.save(user);
        doReturn(user).when(userService).getLoggedUser();
    }

    private void generateCurrentCompWithEntries(int numberOfEntries) {
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 2; i++) {
            LocalDate monday = today
                    .minusWeeks(i)
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            LocalDate sunday = monday.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

            String theme = "Lunch 2 Electric Boogaloo";
            competitionRepository.save(new Competition(theme, monday, sunday));
        }

        Competition competition = competitionRepository.getCurrentCompetition();
        for (int i = 0; i < numberOfEntries; i++) {
            User temp = new User("generic" + i + "@gmail.com", "P4$$word",
                    "",
                    "");
            temp.revokeAuthority("ROLE_UNVERIFIED");
            temp.grantAuthority("ROLE_USER");
            userRepository.save(temp);
            CompetitionDesign design = new CompetitionDesign("Design Designer" + i,
                    "Wow!", "", competition, temp);
            design.setSubmitted(true);
            competition.addEntry(design);
        }
        competitionRepository.save(competition);
    }

    @Test
    void competitionDoesntExists_competitionDetails_returnsPopulatedTemplate() throws Exception {

        LocalDate endDate = LocalDate.now().plusDays(1);
        Competition targetCompetition = competitionRepository.save(
                new Competition("Test Competition", LocalDate.of(2025, 8, 25),
                        endDate));

        mockMvc.perform(get("/competitionDetails/" + targetCompetition.getId()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("competition", targetCompetition))
                .andExpect(view().name("competitions/competitionDetails"));
    }

    @Test
    @WithMockUser(username = "jane@example.com", roles = "USER")
    void competitionEntriesPaginated_competitionEntriesPage_returnsOnePageOfResults()
            throws Exception {
        generateCurrentCompWithEntries(16);

        mvcResult = mockMvc.perform(
                        get("/competitionDetails/" + competitionRepository.getCurrentCompetition().getId()
                                + "/paged")
                                .with(csrf())
                                .param("pageNum", "0")
                                .param("pageSize", "8"))
                .andExpect(status().isOk())
                .andExpect(view().name("competitions/competitionEntriesPageable")).andReturn();

        @SuppressWarnings("unchecked")
        List<CompetitionDesign> designList = (List<CompetitionDesign>) mvcResult.getModelAndView()
                .getModel().get("designs");
        Integer totalPages = (Integer) mvcResult.getModelAndView().getModel().get("totalPages");
        assertEquals(8, designList.size());
        assertEquals(2, totalPages);
    }

    @Test
    @WithMockUser(username = "jane@example.com", roles = "USER")
    void notEnoughEntriesForFullLastPage_competitionEntriesPage_returnsLastPageWithEntriesLessThanPageSize()
            throws Exception {
        generateCurrentCompWithEntries(17);
        Competition competition = competitionRepository.getCurrentCompetition();
        Long competitionId = competition.getId();

        mvcResult = mockMvc.perform(get("/competitionDetails/" + competitionId
                        + "/paged")  // Use actual ID
                        .param("pageNum", "2")
                        .param("pageSize", "8"))
                .andExpect(status().isOk())
                .andExpect(view().name("competitions/competitionEntriesPageable")).andReturn();

        @SuppressWarnings("unchecked")
        List<CompetitionDesign> designList = (List<CompetitionDesign>) mvcResult.getModelAndView()
                .getModel().get("designs");
        Integer totalPages = (Integer) mvcResult.getModelAndView().getModel().get("totalPages");
        assertEquals(1, designList.size());
        assertEquals(3, totalPages);
    }

    @Test
    @WithMockUser(username = "jane@example.com", roles = "USER")
    void usersHaveUnsubmittedEntries_competitionEntriesPage_dontReturnUnsubmittedEntries()
            throws Exception {
        generateCurrentCompWithEntries(4);

//        Set up, add other users with unsubmitted designs on current competition.
        Competition competition = competitionRepository.getCurrentCompetition();
        User otherUser = new User("steve@example.com", "P4$$word", "Steve", "Doe");
        User anotherOtherUser = new User("sarah@example.com", "P4$$word", "Sarah", "Doe");
        user = userRepository.save(otherUser);
        user = userRepository.save(anotherOtherUser);
        CompetitionDesign design1 = new CompetitionDesign("Unsubmitted Design", "Wow!", "",
                competition, otherUser);
        design1.setSubmitted(false);
        CompetitionDesign design2 = new CompetitionDesign("Unsubmitted Design", "Wow!", "",
                competition, otherUser);
        design2.setSubmitted(false);
        competition.addEntry(design1);
        competition.addEntry(design2);
        Competition comp = competitionRepository.save(competition);

        mvcResult = mockMvc.perform(get("/competitionDetails/" + comp.getId() + "/paged")
                        .with(csrf())
                        .param("pageNum", "0")
                        .param("pageSize", "8"))
                .andExpect(status().isOk())
                .andExpect(view().name("competitions/competitionEntriesPageable")).andReturn();

        @SuppressWarnings("unchecked")
        List<CompetitionDesign> designList = (List<CompetitionDesign>) mvcResult.getModelAndView()
                .getModel().get("designs");
        assertEquals(4, designList.size());
    }

    @Test
    @WithMockUser(username = "jane@example.com", roles = "USER")
    void sortByVotesAsc_competitionEntriesPage_correctOrder() throws Exception {
        generateCurrentCompWithEntries(0);
        Competition competition = competitionRepository.getCurrentCompetition();
        Long competitionId = competition.getId();

        User otherUser = new User("steve@example.com", "P4$$word", "Steve", "Doe");
        User anotherOtherUser = new User("sarah@example.com", "P4$$word", "Sarah", "Doe");
        otherUser = userRepository.save(otherUser);
        anotherOtherUser = userRepository.save(anotherOtherUser);

        CompetitionDesign design1 = new CompetitionDesign("Top votes", "", "", competition,
                userService.getLoggedUser());
        CompetitionDesign design2 = new CompetitionDesign("Medium votes", "", "", competition,
                otherUser);
        CompetitionDesign design3 = new CompetitionDesign("No votes", "", "", competition,
                anotherOtherUser);
        design1.setVoteCount(3);
        design2.setVoteCount(2);
        design3.setVoteCount(0);
        design1.setSubmitted(true);
        design2.setSubmitted(true);
        design3.setSubmitted(true);
        competition.addEntry(design1);
        competition.addEntry(design2);
        competition.addEntry(design3);
        competitionRepository.save(competition);

        mvcResult = mockMvc.perform(get("/competitionDetails/" + competitionId
                        + "/paged")  // Use actual ID
                        .param("pageNum", "0")
                        .param("pageSize", "8")
                        .param("sortBy", "VOTES_ASC"))
                .andExpect(status().isOk())
                .andExpect(view().name("competitions/competitionEntriesPageable")).andReturn();

        @SuppressWarnings("unchecked")
        List<CompetitionDesign> designList = (List<CompetitionDesign>) mvcResult.getModelAndView()
                .getModel().get("designs");
        assertEquals(3, designList.size());
        assertEquals(0, designList.get(0).getVoteCount());
        assertEquals(2, designList.get(1).getVoteCount());
        assertEquals(3, designList.get(2).getVoteCount());
    }

}
