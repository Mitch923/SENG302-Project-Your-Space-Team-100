package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.SharedHttpSessionConfigurer.sharedHttpSession;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.SearchQuery;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class U17_SearchingRenovations {

    @Autowired
    public MockMvc mockMvc;

    @Autowired
    public RenovationService renovationService;

    @Autowired
    public UserService userService;

    @Autowired
    WebApplicationContext webApplicationContext;

    private String searchString;

    private int renovationsPerPage;

    private int page;

    private User john;

    private ResultActions resultActions;

    private MockHttpSession session;

    @Given("I enter a search {string} in the search bar")
    public void IEnterASearchStringInTheSearchBar(String searchString) {
        john = userService.getUserByEmail("john@example.com").get();
        this.searchString = searchString;
        this.page = 1;
        this.renovationsPerPage = 10;
    }

    @Given("Session is created")
    public void SessionIsCreated() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(sharedHttpSession()).build();
    }

    @Given("I enter a search {string} that has no matches")
    public void IEnterASearchThatHasNoMatches(String searchString) {
        john = userService.getUserByEmail("john@example.com").get();
        this.searchString = searchString;
        this.page = 1;
        this.renovationsPerPage = 10;
    }

    @Given("There are more records than the screen can handle")
    public void ThereAreMoreRecordsThanTheScreenCanHandle() {
        john = userService.getUserByEmail("john@example.com").get();
        this.renovationsPerPage = 3;
    }

    @Given("I see the list of records, and pagination numbers with search {string}")
    public void ISeeTheListOfRecordsAndPaginationNumbersWithSearch(String searchString)
            throws Exception {
        john = userService.getUserByEmail("john@example.com").get();
        this.searchString = searchString;
        this.page = 1;
        this.renovationsPerPage = 5;

        resultActions = mockMvc.perform(get("/searchMyRenovations")
                .param("query", searchString)
                .param("resultsPerPage", String.valueOf(renovationsPerPage))
                .param("page", String.valueOf(page)));

        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("emptyResult", false));
    }

    @When("I submit my search")
    public void ISubmitMySearch() throws Exception {
        resultActions = mockMvc.perform(get("/getMyRenovationsSearch")
                .param("query", searchString)
                .param("resultsPerPage", String.valueOf(renovationsPerPage))
                .param("page", String.valueOf(page)));
    }

    @When("I click on a page number {int}")
    public void IClickOnAPageNumber(int page) throws Exception {
        resultActions = mockMvc.perform(get("/getMyRenovationsSearch")
                .param("page", String.valueOf(page))
                .param("resultsPerPage", String.valueOf(renovationsPerPage)));
    }

    @Then("I am shown only my renovation records")
    public void IAmShownOnlyMyRenovationRecords() {
        Map<String, Object> model = resultActions.andReturn().getModelAndView().getModel();
        List<RenovationRecord> searchResults = (List<RenovationRecord>) model.get("searchResults");
        searchResults.forEach(renovationRecord -> {

            assertTrue(Objects.equals(john.getId(), renovationRecord.getUser().getId()) || renovationRecord.isPublicRecord());
        });
    }

    @Then("I am shown only renovation records whose name or description include my search values")
    public void IAmOnlyRenovationRecordsWhoseNameOrDescriptionIncludeMySearchValues() {
        Map<String, Object> model = resultActions.andReturn().getModelAndView().getModel();
        List<RenovationRecord> searchResults = (List<RenovationRecord>) model.get("searchResults");
        searchResults.forEach(renovationRecord -> {
            String lowerSearchString = searchString.toLowerCase();
            String lowerDescription = renovationRecord.getDescription().toLowerCase();
            String lowerName = renovationRecord.getName().toLowerCase();

            boolean descriptionMatch = lowerDescription.contains(lowerSearchString);
            boolean nameMatch = lowerName.contains(lowerSearchString);
            assertTrue(descriptionMatch | nameMatch);
        });
    }

    @Then("There are no results")
    public void ThereAreNoResults() {
        Map<String, Object> model = resultActions.andReturn().getModelAndView().getModel();
        List<RenovationRecord> searchResults = (List<RenovationRecord>) model.get("searchResults");
        assertTrue(searchResults.isEmpty());
    }

    @Then("The results are split into pages")
    public void TheResultsAreSplitIntoPages() {

        SearchQuery searchQuery = (SearchQuery) resultActions.andReturn().getRequest().getSession()
                .getAttribute("searchMyRenovationsSearchQuery");
        Assertions.assertNotNull(searchQuery);
        assertTrue(searchQuery.getTotalPages() > 1);
    }

    @Then("I see the list of renovation records corresponding to that page number {int}")
    public void ISeeTheListOfRenovationRecordsCorrespondToThatPageNumber(int pageNumber) {
        Map<String, Object> model = resultActions.andReturn().getModelAndView().getModel();
        List<RenovationRecord> searchResults = (List<RenovationRecord>) model.get("searchResults");
        List<Long> searchResultIds = searchResults.stream().map(RenovationRecord::getId).toList();
        List<RenovationRecord> allResults = renovationService.searchRenovationRecords(searchString,
                john, Integer.MAX_VALUE, 1).getContent();
        List<Long> allResultIds = allResults.stream().map(RenovationRecord::getId).toList();

        searchResultIds.forEach(resultId -> {
            int indexInResult = allResultIds.indexOf(resultId);
            int pageStart = (pageNumber - 1) * renovationsPerPage;
            int pageEnd = pageStart + renovationsPerPage - 1;

            assertTrue(indexInResult >= pageStart && indexInResult <= pageEnd);
        });
    }
}
