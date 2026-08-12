package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDate;
import nz.ac.canterbury.seng302.homehelper.entity.Competition;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionDesignService;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationDesignService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Transactional
public class U10013_DisplayCompetitionEntriesFeature {

    private MvcResult httpResponse;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CompetitionService competitionService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RenovationDesignService renovationDesignService;
    @Autowired
    private RenovationService renovationService;
    @Autowired
    private CompetitionDesignService competitionDesignService;

    private Competition currentCompetition;

    private Long currentCompetitionId;

    @Before
    public void before() {
        LocalDate endDate = LocalDate.now().plusDays(1);
        currentCompetition = competitionService.save(
                new Competition("Test Competition", LocalDate.of(2025, 8, 25),
                        endDate));
    }

    @Given("I am on the home page")
    public void i_am_on_the_home_page() throws Exception {
        mockMvc.perform(
                        get("/home"))
                .andExpect(status().isOk());
    }

    @When("I click the view all button for the currently open competition")
    public void i_click_the_view_all_button_for_the_currently_open_competition() throws Exception {
        LocalDate endDate = LocalDate.now().plusDays(1);
        Competition currentCompetition = competitionService.save(
                new Competition("Test Competition", LocalDate.of(2025, 8, 25), endDate)
        );
        currentCompetitionId = currentCompetition.getId();

        httpResponse = mockMvc.perform(
                        MockMvcRequestBuilders.get("/competitionDetails/" + currentCompetitionId)
                                .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("I am taken to the competition details page")
    public void i_am_taken_to_the_competition_details_page() {
        String viewName = httpResponse.getModelAndView().getViewName();
        org.junit.jupiter.api.Assertions.assertEquals("competitions/competitionDetails", viewName);
    }

    @Then("I can see the first page of designs")
    public void i_can_see_the_first_page_of_designs() throws Exception {
        MvcResult entriesResponse = mockMvc.perform(
                        MockMvcRequestBuilders.get("/competitionDetails/" + currentCompetitionId + "/paged")
                                .param("pageNum", "0")
                                .param("pageSize", "10")
                                .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        String viewName = entriesResponse.getModelAndView().getViewName();
        org.junit.jupiter.api.Assertions.assertEquals("competitions/competitionEntriesPageable",
                viewName);
    }


}
