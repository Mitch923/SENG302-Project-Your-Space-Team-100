package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.SearchQuery;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class U19_SharingRenovationsFeature {

    @Autowired
    public MockMvc mockMvc;

    @Autowired
    public UserRepository userRepository;
    @Autowired
    public UserService userService;
    @Autowired
    public RenovationService renovationService;
    @Autowired
    AuthenticationProvider authenticationProvider;
    private ResultActions resultActions;

    private int renovationsPerPageCount;

    private int pageNumber;

    private RenovationRecord record;

    @Given("I am on the renovation record details page for a record I own")
    public void IAmOnTheRovationRecordDetailsPageForARecordIOwn() throws Exception {
        record = renovationService.getRenovationRecordById(1L);
        resultActions = mockMvc.perform(get("/viewRenovation/" + record.getId()))
                .andExpect(status().isOk());

        Map<String, Object> model = resultActions.andReturn().getModelAndView().getModel();
        RenovationRecord viewedRecord = (RenovationRecord) model.get("renovationRecord");
        assertEquals(viewedRecord.getId(), record.getId());
    }

    @Given("The record is not marked as public")
    public void TheRecordIsNotMarkedAsPublic() throws Exception {
        renovationService.setVisibility(record.getId(), false);
        record = renovationService.getRenovationRecordById(record.getId());
        assertFalse(record.isPublicRecord());
    }

    @Given("I see the list of public renovation records, and pagination numbers")
    public void ISeeTheListOfPublicRenovationRecordsAndPaginationNumbers() throws Exception {
        pageNumber = 1;
        renovationsPerPageCount = 2;
    }

    @Given("I see the list of public renovation records")
    public void ISeeTheListOfPublicRenovationRecords() throws Exception {
        resultActions = mockMvc.perform(get("/publicRenovations"));

        Map<String, Object> model = resultActions.andReturn().getModelAndView().getModel();
        List<RenovationRecord> publicRenovationRecords = (List<RenovationRecord>) model.get(
                "renovations");
        assertFalse(publicRenovationRecords.isEmpty());
    }

    @Given("I am viewing the list of public renovation records")
    public void IAmViewingTheListOfPublicRenovationRecords() throws Exception {
        mockMvc.perform(get("/publicRenovations"))
                .andExpect(status().isOk());
    }

    @When("I toggle on a toggle labelled 'Public'")
    public void IToggleOnAToggleLabelledPublic() throws Exception {
        resultActions = mockMvc.perform(post("/viewRenovation/" + record.getId() + "/setVisibility")
                .param("visibility", String.valueOf(true))
                .with(csrf()));
    }

    @When("I toggle off the toggle labelled 'Public'")
    public void IToggleOffTheToggleLabelledPublic() throws Exception {
        resultActions = mockMvc.perform(post("/viewRenovation/" + record.getId() + "/setVisibility")
                .param("visibility", String.valueOf(false))
                .with(csrf()));
    }

    @When("There are too many records to be shown on a single page")
    public void ThereAreTooManyRecordsToBeShownOnASinglePage() throws Exception {
        renovationsPerPageCount = 1;
        pageNumber = 1;
    }

    @When("I click the 'Browse renovations' button")
    public void IClickTheBrowseRenovationsButton() throws Exception {
        resultActions = mockMvc.perform(get("/getPublicRenovations")
                .param("page", String.valueOf(1))
                .param("count", String.valueOf(Integer.MAX_VALUE)));
    }

    @When("There are more than 10 pages of public renovation records")
    public void ThereAreMoreThan10PagesOfPublicRenovationRecords() throws Exception {
        resultActions = mockMvc.perform(get("/getPublicRenovations")
                .param("page", String.valueOf(pageNumber))
                .param("count", String.valueOf(renovationsPerPageCount)));

        MockHttpSession session = (MockHttpSession) resultActions.andReturn().getRequest()
                .getSession();
        SearchQuery searchQuery = (SearchQuery) session.getAttribute("renovationSearchQuery");
        assertTrue(searchQuery.getTotalPages() > 10);
    }

    @When("I input a {int} within the range of available pages of public renovation records")
    public void IInputAPageNumberWithinTheRangeOfAvailablePagesOfPublicRenovationRecords(
            int pageNumber) throws Exception {
        this.pageNumber = pageNumber;
    }

    @When("I confirm that I want to go to that page number of public renovation records")
    public void IConfirmThatIWantToGoToPublicRenovationRecords() throws Exception {
        resultActions = mockMvc.perform(get("/getPublicRenovations")
                .param("page", String.valueOf(pageNumber))
                .param("count", String.valueOf(renovationsPerPageCount)));
    }

    @When("I click on a renovation record with name {string}")
    public void IClickOnARenovationRecordWithName(String name) throws Exception {
        RenovationRecord clickedRecord = renovationService.getRenovationRecordByName(name);
        resultActions = mockMvc.perform(get("/viewRenovation/" + clickedRecord.getId()));
    }

    @Then("My renovation record will be visible in search results for all logged in users")
    public void MyRenovationRecordWillBeVisibleInSearchResultsForAllLoggedInUsers()
            throws Exception {
        resultActions.andExpect(status().isOk());
        record = renovationService.getRenovationRecordById(record.getId());
        assertTrue(record.isPublicRecord());

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                "jane@example.com", "P4$$word");
        Authentication authenticated = authenticationProvider.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(authenticated);
        resultActions = mockMvc.perform(get("/getPublicRenovations")
                        .param("page", String.valueOf(1))
                        .param("count", String.valueOf(Integer.MAX_VALUE)))
                .andExpect(status().isOk());

        Map<String, Object> model = resultActions.andReturn().getModelAndView().getModel();
        List<RenovationRecord> publicRenovations = (List<RenovationRecord>) model.get(
                "publicRenovations");
        List<Long> publicRenovationIds = publicRenovations.stream().map(RenovationRecord::getId)
                .toList();
        assertTrue(publicRenovationIds.contains(record.getId()));
    }

    @Then("It will remain visible in my search results")
    public void ItWillRemainVisibleInMySearchResults() throws Exception {
        resultActions.andExpect(status().isOk());
        resultActions = mockMvc.perform(get("/getMyRenovationsSearch")
                        .param("page", String.valueOf(1))
                        .param("resultsPerPage", String.valueOf(Integer.MAX_VALUE))
                        .param("query", ""))
                .andExpect(status().isOk());
        Map<String, Object> model = resultActions.andReturn().getModelAndView().getModel();
    }

    @Then("My renovation record will no longer be visible in search results from other users")
    public void MyRenovationRecordWillNotBeVisibleInSearchResultsFromOtherUsers() throws Exception {
        record = renovationService.getRenovationRecordById(record.getId());
        assertFalse(record.isPublicRecord());

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                "jane@example.com", "P4$$word");
        Authentication authenticated = authenticationProvider.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(authenticated);
        resultActions = mockMvc.perform(get("/getPublicRenovations")
                        .param("page", String.valueOf(1))
                        .param("count", String.valueOf(Integer.MAX_VALUE)))
                .andExpect(status().isOk());

        Map<String, Object> model = resultActions.andReturn().getModelAndView().getModel();
        List<RenovationRecord> publicRenovations = (List<RenovationRecord>) model.get(
                "publicRenovations");
        List<Long> publicRenovationIds = publicRenovations.stream().map(RenovationRecord::getId)
                .toList();
        assertFalse(publicRenovationIds.contains(record.getId()));
    }

    @Then("The list of records is divided in sub-lists with pagination numbers")
    public void TheListOfRecordsIsDividedInSubListsWithPaginationNumbers() throws Exception {
        MvcResult result = mockMvc.perform(get("/getPublicRenovations")
                        .param("count", String.valueOf(renovationsPerPageCount))
                        .param("page", String.valueOf(pageNumber)))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> model = result.getModelAndView().getModel();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession();
        SearchQuery searchQuery = (SearchQuery) session.getAttribute("renovationSearchQuery");
        List<RenovationRecord> publicRenovations = (List<RenovationRecord>) model.get(
                "publicRenovations");
        List<RenovationRecord> allPublicRenovations = renovationService.searchRenovationRecords("",
                null, Integer.MAX_VALUE, 1).getContent();

        assertFalse(publicRenovations.isEmpty());
        assertTrue(searchQuery.getTotalPages() > 1);
        assertTrue(publicRenovations.size() < allPublicRenovations.size());
    }

    @Then("I see a list of public renovation records")
    public void ISeeAListOfPublicRenovationRecords() throws Exception {
        Map<String, Object> model = resultActions.andReturn().getModelAndView().getModel();
        List<RenovationRecord> publicRenovations = (List<RenovationRecord>) model.get(
                "publicRenovations");
        publicRenovations.forEach(record -> {
            assertTrue(record.isPublicRecord());
        });
    }

    @Then("The records are sorted by more recently created ones in descending order")
    public void TheRecordsAreSortedByMoreRecentlyCreatedOnesInDescendingOrder() throws Exception {
        Map<String, Object> model = resultActions.andReturn().getModelAndView().getModel();
        List<RenovationRecord> publicRenovations = (List<RenovationRecord>) model.get(
                "publicRenovations");
        List<RenovationRecord> orderedRenovations = publicRenovations.stream()
                .sorted(Comparator.comparing(RenovationRecord::getId))
                .toList()
                .reversed();
        for (int i = 0; i < publicRenovations.size(); i++) {
            assertEquals(orderedRenovations.get(i).getId(), publicRenovations.get(i).getId());
        }
    }

    @Then("I see the list of public renovation records corresponding to that {int}")
    public void ISeeTheListOfPublicRenovationRecordsCorrespondToThat(int page) throws Exception {
        Map<String, Object> model = resultActions.andReturn().getModelAndView().getModel();
        List<RenovationRecord> publicRenovations = (List<RenovationRecord>) model.get(
                "publicRenovations");
        List<Long> publicRenovationIds = publicRenovations.stream().map(RenovationRecord::getId)
                .toList();
        List<RenovationRecord> allPublicRenovations = renovationService.searchRenovationRecords("",
                null, Integer.MAX_VALUE, 1).getContent();
        List<Long> allPublicRenovationIds = allPublicRenovations.stream()
                .map(RenovationRecord::getId).toList();

        int pageStart = (page - 1) * renovationsPerPageCount;
        int pageEnd = pageStart + renovationsPerPageCount - 1;

        publicRenovationIds.forEach(id -> {
            int indexInAll = allPublicRenovationIds.indexOf(id);
            assertTrue(indexInAll >= pageStart);
            assertTrue(indexInAll <= pageEnd);
        });
    }

    @Then("I see the details of that renovation record with name {string}")
    public void ISeeTheDetailsOfThatRenovationRecordWithName(String name) throws Exception {
        resultActions.andExpect(status().isOk());
        Map<String, Object> model = resultActions.andReturn().getModelAndView().getModel();
        RenovationRecord renovationViewed = (RenovationRecord) model.get("renovationRecord");
        assertEquals(name, renovationViewed.getName());
    }
}
