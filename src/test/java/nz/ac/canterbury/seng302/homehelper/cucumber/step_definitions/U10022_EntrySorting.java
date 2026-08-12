package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import java.util.Objects;
import nz.ac.canterbury.seng302.homehelper.entity.CompetitionDesign;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class U10022_EntrySorting {

    @Autowired
    private MockMvc mockMvc;
    private MvcResult mvcResult;
    @Autowired
    private CompetitionService competitionService;

    @Given("I am on the competition details page for the current competition")
    public void iAmOnTheCompetitionDetailsPageForTheCurrentCompetition() throws Exception {
        mockMvc.perform(
                        get("/competitionDetails/" + competitionService.getCurrentCompetition().getId())
                                .with(csrf()))
                .andExpect(status().isOk());
    }

    @When("I select the {string} option from the sort by dropdown")
    public void iSelectTheOptionFromTheSortByDropdown(String sortBy) throws Exception {
        String sortByOption = switch (sortBy) {
            case "Least Votes" -> "VOTES_ASC";
            case "Alphabetical (A-Z)" -> "NAME_ASC";
            case "Alphabetical (Z-A)" -> "NAME_DESC";
            default -> "VOTES_DESC";
        };
        mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                        "/competitionDetails/" + competitionService.getCurrentCompetition().getId()
                                                + "/paged")
                                .param("pageNum", "0")
                                .param("pageSize", "8")
                                .param("sortBy", sortByOption)
                                .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("the designs are displayed in descending order by vote count with the highest voted design shown first")
    public void theDesignsAreDisplayedInDescendingOrderByVoteCountWithTheHighestVotedDesignShownFirst() {
        @SuppressWarnings("unchecked")
        List<CompetitionDesign> designs = (List<CompetitionDesign>) Objects.requireNonNull(
                mvcResult.getModelAndView()).getModel().get("designs");
        Assertions.assertTrue(designs.getFirst().getVoteCount() > designs.get(1).getVoteCount());
    }

    @Then("the designs are displayed in ascending order by vote count with the lowest voted design shown first")
    public void theDesignsAreDisplayedInAscendingOrderByVoteCountWithTheLowestVotedDesignShownFirst() {
        @SuppressWarnings("unchecked")
        List<CompetitionDesign> designs = (List<CompetitionDesign>) Objects.requireNonNull(
                mvcResult.getModelAndView()).getModel().get("designs");
        Assertions.assertTrue(designs.getFirst().getVoteCount() <= designs.get(1).getVoteCount());
    }

    @Then("the designs are displayed in ascending alphabetical order by design name")
    public void theDesignsAreDisplayedInAscendingAlphabeticalOrderByDesignName() {
        @SuppressWarnings("unchecked")
        List<CompetitionDesign> designs = (List<CompetitionDesign>) Objects.requireNonNull(
                mvcResult.getModelAndView()).getModel().get("designs");
        Assertions.assertTrue(
                designs.getFirst().getName().compareToIgnoreCase(designs.get(1).getName()) <= 0);
    }

    @Then("the designs are displayed in descending alphabetical order by design name")
    public void theDesignsAreDisplayedInDescendingAlphabeticalOrderByDesignName() {
        @SuppressWarnings("unchecked")
        List<CompetitionDesign> designs = (List<CompetitionDesign>) Objects.requireNonNull(
                mvcResult.getModelAndView()).getModel().get("designs");
        Assertions.assertTrue(
                designs.getFirst().getName().compareToIgnoreCase(designs.get(1).getName()) >= 0);
    }

    @When("go to the competition details page for the current competition")
    public void goToTheCompetitionDetailsPageForTheCurrentCompetition() throws Exception {
        mockMvc.perform(
                        get("/competitionDetails/" + competitionService.getCurrentCompetition().getId())
                                .with(csrf()))
                .andExpect(status().isOk());
        mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                        "/competitionDetails/" + competitionService.getCurrentCompetition().getId()
                                                + "/paged")
                                .param("pageNum", "0")
                                .param("pageSize", "8")
                                .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("by default the designs are displayed in descending order by vote count with the highest voted design shown first")
    public void byDefaultTheDesignsAreDisplayedInDescendingOrderByVoteCountWithTheHighestVotedDesignShownFirst() {
        @SuppressWarnings("unchecked")
        List<CompetitionDesign> designs = (List<CompetitionDesign>) Objects.requireNonNull(
                mvcResult.getModelAndView()).getModel().get("designs");
        Assertions.assertTrue(designs.getFirst().getVoteCount() > designs.get(1).getVoteCount());
    }
}
