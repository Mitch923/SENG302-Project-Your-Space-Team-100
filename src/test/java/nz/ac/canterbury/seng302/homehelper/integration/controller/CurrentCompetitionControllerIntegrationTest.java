package nz.ac.canterbury.seng302.homehelper.integration.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.Optional;
import nz.ac.canterbury.seng302.homehelper.entity.Competition;
import nz.ac.canterbury.seng302.homehelper.entity.CompetitionDesign;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.CompetitionDesignRepository;
import nz.ac.canterbury.seng302.homehelper.repository.CompetitionRepository;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CurrentCompetitionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @SpyBean
    private UserService userService;
    @MockBean
    private CompetitionDesignRepository competitionDesignRepository;
    @MockBean
    private CompetitionRepository competitionRepository;

    private User user;
    private Competition currentCompetition;

    @BeforeEach
    void before() {
        user = new User("jane@example.com", "P4$$word", "John", "Doe");
        user.setId(1L);
        currentCompetition = new Competition("Mock Competition", LocalDate.of(1, 1, 1),
                LocalDate.of(1, 1, 7));
        currentCompetition.setId(1L);

        doReturn(user).when(userService).getLoggedUser();
    }

    @Test
    @WithMockUser(username = "jane@example.com", roles = "USER")
    void userDoesntHaveEntry_createCompetitionEntry_newCompetitionEntryCreated() throws Exception {
        when(competitionDesignRepository.getByCompetitionIdAndUserId(1L, user.getId())).thenReturn(
                null);
        when(competitionRepository.getCurrentCompetition()).thenReturn(currentCompetition);
        when(competitionDesignRepository.save(any(CompetitionDesign.class))).thenAnswer(i -> {
            CompetitionDesign design = i.getArgument(0);
            design.setId(1L);
            return design;
        });
        mockMvc.perform(post("/createCompetitionEntry").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/editCompetitionEntry/1"));

        ArgumentCaptor<CompetitionDesign> captor = ArgumentCaptor.forClass(CompetitionDesign.class);
        verify(competitionDesignRepository).save(captor.capture());
        CompetitionDesign design = captor.getValue();
        assertEquals("John's Design", design.getName());
    }

    @Test
    @WithMockUser(username = "jane@example.com", roles = "USER")
    void userDoesHaveEntry_createCompetitionEntry_httpForbidden() throws Exception {
        doReturn(new CompetitionDesign()).when(
                competitionDesignRepository).getByCompetitionIdAndUserId(1L, user.getId());
        when(competitionRepository.getCurrentCompetition()).thenReturn(currentCompetition);

        mockMvc.perform(post("/createCompetitionEntry").with(csrf()))
                .andExpect(status().isForbidden());

        verify(competitionDesignRepository, never()).save(any(CompetitionDesign.class));
    }

    @Test
    @WithMockUser(username = "jane@example.com", roles = "USER")
    void userNotVoted_voteForEntry_voteAdded() throws Exception {
        LocalDate startDate = LocalDate.now().minusWeeks(1);
        LocalDate endDate = LocalDate.now().plusDays(1);
        Competition currentCompetition = new Competition("Test Competition", startDate, endDate);
        User josh = new User("josh@email.com", "P4$$word", "josh", "Doe");
        josh.setId(2L);
        CompetitionDesign competitionDesign = new CompetitionDesign("Design", "", "",
                currentCompetition,
                josh);
        competitionDesign.setSubmitted(true);
        doReturn(Optional.of(competitionDesign)).when(competitionDesignRepository).findById(1L);
        doReturn(currentCompetition).when(competitionRepository).getCurrentCompetition();

        mockMvc.perform(post("/toggleEntryVote/" + 1L).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "jane@example.com", roles = "USER")
    void userOwnsDesign_voteForEntry_forbiddenResponseReturned() throws Exception {
        LocalDate startDate = LocalDate.now().minusWeeks(1);
        LocalDate endDate = LocalDate.now().plusDays(1);
        Competition currentCompetition = new Competition("Test Competition", startDate, endDate);

        CompetitionDesign competitionDesign = new CompetitionDesign("Design", "", "",
                currentCompetition,
                user);

        doReturn(Optional.of(competitionDesign)).when(competitionDesignRepository).findById(1L);
        doReturn(currentCompetition).when(competitionRepository).getCurrentCompetition();

        mockMvc.perform(post("/toggleEntryVote/" + 1L).with(csrf()))
                .andExpect(status().isForbidden());
    }
}
