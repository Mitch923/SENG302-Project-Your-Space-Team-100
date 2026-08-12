package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import java.util.Map;
import nz.ac.canterbury.seng302.homehelper.entity.Competition;
import nz.ac.canterbury.seng302.homehelper.entity.CompetitionDesign;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.CompetitionRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class U10019_VotingOnDesigns {

    private ResultActions resultActions;
    private Long currentCompetitionId;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CompetitionService competitionService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RenovationRecordRepository renovationRecordRepository;
    @Autowired
    private CompetitionRepository competitionRepository;
    @Autowired
    private RenovationService renovationService;

    @Given("The system contains the default users, renovations, and competitions")
    public void the_system_contains_the_default_users_renovations_and_competitions() {
        userRepository.deleteAll();
        userRepository.flush();

        renovationRecordRepository.deleteAll();
        renovationRecordRepository.flush();

        competitionRepository.deleteAll();
        competitionRepository.flush();

        List<User> users = userService.createDefaultUsers(10);
        renovationService.createDefaultRenovations(users);
        competitionService.createDefaultCompetitions(10);
    }


    @Given("I am on the Home page")
    @When("I go to the home page")
    public void i_am_on_the_home_page() throws Exception {
        resultActions = mockMvc.perform(get("/home")).andExpect(status().isOk());

        currentCompetitionId = competitionService.getCurrentCompetition().getId();
    }

    @Given("I am logged in")
    public void i_am_logged_in() {
        assertNotNull(userService.getLoggedUser());
    }

    @When("I navigate to the competition details page")
    public void i_navigate_to_the_competition_details_page() throws Exception {
        resultActions = mockMvc.perform(get("/competitionDetails/" + currentCompetitionId))
                .andExpect(status().isOk());
    }

    @Then("the 1st, 2nd, and 3rd place designs are displayed prominently at the top of the competition page")
    public void the_1st_2nd_and_3rd_place_designs_are_displayed_prominently_at_the_top_of_competition_page() {
        Map<String, Object> model = resultActions.andReturn().getModelAndView().getModel();

        CompetitionDesign[] expectedDesigns = competitionService.getTopCompetitionDesignsByCompetition(
                currentCompetitionId, 3);

        assertEquals(expectedDesigns[0].getName(),
                ((CompetitionDesign) model.get("firstPlace")).getName());
        assertEquals(expectedDesigns[1].getName(),
                ((CompetitionDesign) model.get("secondPlace")).getName());
        assertEquals(expectedDesigns[2].getName(),
                ((CompetitionDesign) model.get("thirdPlace")).getName());
    }

    @Then("I can see the current weekly competition and its current top 3 entries")
    public void i_can_see_the_current_weekly_competition_and_its_current_top_3_entries() {
        Map<String, Object> model = resultActions.andReturn().getModelAndView().getModel();

        CompetitionDesign[] expectedDesigns = competitionService.getTopCompetitionDesignsByCompetition(
                currentCompetitionId, 3);

        assertEquals(competitionService.getCurrentCompetition().getTheme(),
                ((Competition) model.get("weeklyCompetition")).getTheme());
        assertEquals(expectedDesigns[0].getName(),
                ((CompetitionDesign) model.get("currentFirst")).getName());
        assertEquals(expectedDesigns[1].getName(),
                ((CompetitionDesign) model.get("currentSecond")).getName());
        assertEquals(expectedDesigns[2].getName(),
                ((CompetitionDesign) model.get("currentThird")).getName());
    }
}
