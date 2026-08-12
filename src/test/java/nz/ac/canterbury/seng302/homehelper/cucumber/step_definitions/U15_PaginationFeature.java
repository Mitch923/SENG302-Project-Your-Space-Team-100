package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.SharedHttpSessionConfigurer.sharedHttpSession;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationDesign;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.SearchQuery;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class U15_PaginationFeature {

    @Autowired
    public MockMvc mockMvc;
    @Autowired
    public UserRepository userRepository;
    @Autowired
    public UserService userService;
    @Autowired
    public RenovationService renovationService;
    @Autowired
    public RenovationRecordRepository renovationRecordRepository;
    @Autowired
    AuthenticationProvider authenticationProvider;
    @Autowired
    private WebApplicationContext webApplicationContext;
    private int recordToView;

    private ResultActions resultActions;

    private int designPerPageCount;

    private int pageNumber;

    private int expectedPageCount;


    @Given("The system contains the default data")
    public void theSystemContainsTheDefaultData() {
        userRepository.deleteAll();
        userRepository.flush();

        renovationRecordRepository.deleteAll();
        renovationRecordRepository.flush();

        List<User> users = userService.createDefaultUsers(0);
        renovationService.createDefaultRenovations(users);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(sharedHttpSession()).build();
    }

    @Given("I am logged into the account with email {string} and password {string}")
    public void iAmLoggedInToTheAccountWithEmailAndPassword(String email, String password) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                email, password);
        Authentication authenticated = authenticationProvider.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(authenticated);
        assertNotNull(userService.getLoggedUser());
    }

    @Given("there are more designs in my renovation record that my screen size can handle")
    public void thereAreMoreDesignsInMyRenovationRecordThatMyScreenSizeCanHandle() {
        User john = userService.getLoggedUser();
        List<RenovationRecord> records = renovationService.getRenovationRecords(john);

        recordToView = records.get(5).getId().intValue();
        designPerPageCount = 2;

        List<RenovationDesign> renovationDesigns = renovationService.getRenovationRecordById(
                        recordToView)
                .getDesignsForRenovation();
        assertEquals("Renovation Record 6", records.get(5).getName());
        assertTrue(renovationDesigns.size() > designPerPageCount);
    }

    @Given("I see the list of designs, and pagination numbers")
    public void iSeeTheListOfDesignsAndPaginationNumbers() {
        User john = userService.getLoggedUser();
        List<RenovationRecord> records = renovationService.getRenovationRecords(john);

        recordToView = records.get(5).getId().intValue();
        designPerPageCount = 2;
        expectedPageCount = 6;

        List<RenovationDesign> renovationDesigns = renovationService.getRenovationRecordById(
                        recordToView)
                .getDesignsForRenovation();
        assertEquals("Renovation Record 6", records.get(5).getName());
        assertTrue(renovationDesigns.size() > designPerPageCount);
    }

    @Given("I see the list of designs, and pagination numbers, and there are more than 10 pages")
    public void iSeeTheListOfDesignsAndPaginationNumbersAndMoreThan10Pages() {
        User john = userService.getLoggedUser();
        List<RenovationRecord> records = renovationService.getRenovationRecords(john);

        recordToView = records.get(2).getId().intValue();
        designPerPageCount = 2;
        expectedPageCount = 50;

        List<RenovationDesign> renovationDesigns = renovationService.getRenovationRecordById(
                        recordToView)
                .getDesignsForRenovation();
        assertEquals("Renovation Record 3", records.get(2).getName());
        assertTrue(renovationDesigns.size() > designPerPageCount * 10);
    }

    @When("I click on a {int}")
    public void iClickOnA(int pageNumber) throws Exception {
        resultActions = mockMvc.perform(get("/viewRenovation/" + recordToView + "/getDesigns")
                .param("page", String.valueOf(pageNumber))
                .param("resultsPerPage", String.valueOf(designPerPageCount))
                .with(csrf()));
    }

    @When("I see the list of records")
    public void iSeeTheListOfRecords() throws Exception {
        resultActions = mockMvc.perform(get("/viewRenovation/" + recordToView + "/getDesigns")
                .param("page", "1")
                .param("resultsPerPage", String.valueOf(designPerPageCount))
                .with(csrf()));
    }

    @When("I input a {int} within the range of available pages")
    public void iInputAPageNumberWithinTheRangeOfAvailablePages(int pageNumber) throws Exception {
        this.pageNumber = pageNumber;
    }

    @When("I confirm that I want to go to that page")
    public void iConfirmThatIWantToGoToThatPage() throws Exception {
        resultActions = mockMvc.perform(get("/viewRenovation/" + recordToView + "/getDesigns")
                .param("page", String.valueOf(pageNumber))
                .param("resultsPerPage", String.valueOf(designPerPageCount))
                .with(csrf()));
    }

    @Then("the list is divided into sub-lists with pagination number")
    public void theListIsDividedIntoSubListsWithPaginationNumber() throws Exception {
        MockHttpSession session = (MockHttpSession) resultActions.andExpect(status().isOk())
                .andReturn().getRequest().getSession();
        SearchQuery searchQuery = (SearchQuery) session.getAttribute("designSearchQuery");
        Assertions.assertNotNull(searchQuery);
        Assertions.assertEquals(designPerPageCount, searchQuery.getResultsPerPage());
        Assertions.assertEquals(1, searchQuery.getPage());
        Assertions.assertTrue(searchQuery.getTotalPages() > 1);
    }

    @Then("I see the list of designs corresponding to that {int}")
    public void iSeeTheListOfDesignsCorrespondToThat(int pageNumber) throws Exception {
        resultActions.andExpect(status().isOk());
        MockHttpSession session = (MockHttpSession) resultActions.andExpect(status().isOk())
                .andReturn().getRequest().getSession();
        SearchQuery searchQuery = (SearchQuery) session.getAttribute("designSearchQuery");
        Assertions.assertNotNull(searchQuery);
        Assertions.assertEquals(designPerPageCount, searchQuery.getResultsPerPage());
        Assertions.assertEquals(pageNumber, searchQuery.getPage());
        Assertions.assertEquals(expectedPageCount, searchQuery.getTotalPages());

        Map<String, Object> model = Objects.requireNonNull(
                resultActions.andReturn().getModelAndView()).getModel();

        List<RenovationDesign> returnedRenovationDesigns = (List<RenovationDesign>) model.get(
                "renovationDesigns");
        List<Long> returnedDesignIds = returnedRenovationDesigns.stream()
                .map(RenovationDesign::getId).toList();
        List<RenovationDesign> allRecordRenovationDesigns = renovationService.getRenovationRecordById(
                        recordToView)
                .getDesignsForRenovation();
        List<Long> allRecordDesignIds = allRecordRenovationDesigns.stream()
                .map(RenovationDesign::getId).toList();

        returnedDesignIds.forEach(designId -> {
            int indexInAllDesigns = allRecordDesignIds.indexOf(designId);
            assertTrue(indexInAllDesigns >= (pageNumber - 1) * designPerPageCount - 1);
            assertTrue(indexInAllDesigns <= pageNumber * designPerPageCount - 1);
        });
    }
}
