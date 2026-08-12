package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.servlet.ModelAndView;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class U11_EditRenovationFeature {

    private final List<String> roomNames = new ArrayList<>();
    @Autowired
    public MockMvc mockMvc;
    @Autowired
    private RenovationRecordRepository renovationRecordRepository;
    @Autowired
    private UserRepository userRepository;
    private RenovationRecord renovationToEdit = new RenovationRecord("Renovation 1",
            "Renovating the bathrooms");
    private String renovationName = "Renovation 1";
    private String renovationDescription = "Renovation description";
    private MvcResult mvcResult;


    /**
     * Helper method to extract errors from the model view. Asserts that the extracted object is a
     * hashmap before casting.
     *
     * @return HashMap containing error messages
     */
    private HashMap<String, String> getHashMapErrorsFromModel() {
        Object rawErrors = Objects.requireNonNull(mvcResult.getModelAndView())
                .getModel()
                .get("errors");

        Assertions.assertInstanceOf(HashMap.class, rawErrors, "Expected 'errors' to be a HashMap");
        return (HashMap) rawErrors;
    }

    @Given("I am on the renovation record details page")
    public void iAmOnTheRenovationRecordDetailsPage() {
        renovationToEdit.setId(1L);
        renovationToEdit.setUser(
                userRepository.findByEmailIgnoreCase("sarahandjackthompson@gmail.com").get());
        renovationRecordRepository.save(renovationToEdit);
    }

    @When("I click edit")
    public void iClickEdit() throws Exception {
        mvcResult = mockMvc.perform(get("/editRenovation/" + "1")
                        .with(csrf()))
                .andReturn();

    }


    @Then("I am on the edit renovation form with all the details prepopulated")
    public void iAmOnTheEditRenovationFormWithAllTheDetailsPrepopulated() {
        ModelAndView modelAndView = Objects.requireNonNull(mvcResult.getModelAndView());
        RenovationRecord renovation = (RenovationRecord) modelAndView.getModel().get("renovation");

        assertEquals(renovationToEdit.getName(), renovation.getName());
        assertEquals(renovationToEdit.getDescription(), renovation.getDescription());
    }

    @Given("I am on the edit renovation form")
    public void iAmOnTheEditRenovationForm() {
        renovationToEdit = new RenovationRecord("Renovation 1", "Renovating the bathrooms");
        renovationToEdit.setUser(
                userRepository.findByEmailIgnoreCase("sarahandjackthompson@gmail.com").get());
        renovationRecordRepository.save(renovationToEdit);
        renovationName = renovationToEdit.getName();
        renovationDescription = renovationToEdit.getDescription();
    }

    @And("I enter valid values for the name description and a room")
    public void iEnterValidValuesForTheNameDescriptionAndARoom() {
        renovationName = " Renovation 2";
        renovationDescription = "New renovation description";
        roomNames.add("Bathrooms");

    }

    @When("I click submit")
    public void iClickSubmit() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post(
                "/editRenovation/" + renovationRecordRepository.findRenovationByName(
                        renovationToEdit.getName()).getId().toString())
                .param("name", renovationName)
                .param("description", renovationDescription)
                .with(csrf());

        for (String roomName : roomNames) {
            requestBuilder = requestBuilder.param("roomNames", roomName);
            requestBuilder = requestBuilder.param("roomIds", "-1");
        }

        mvcResult = mockMvc.perform(requestBuilder)
                .andReturn();
    }

    @Then("The renovation record details are updated and I am taken to the renovation record page")
    public void theRenovationRecordDetailsAreUpdatedAndIAmTakenToTheRenovationRecordPage() {
        RenovationRecord updatedRenovationRecord = renovationRecordRepository.findById(1L).get();

        Assertions.assertEquals(renovationName, updatedRenovationRecord.getName());
        Assertions.assertEquals(renovationDescription, updatedRenovationRecord.getDescription());
        Assertions.assertEquals(roomNames.getFirst(),
                updatedRenovationRecord.getRooms().getFirst().getName());

        int status = mvcResult.getResponse().getStatus();
        String redirectUrl = mvcResult.getResponse().getRedirectedUrl();

        Assertions.assertTrue(status >= 300 && status < 400,
                "Expected a redirection status, got: " + status);
        Assertions.assertTrue(redirectUrl != null && redirectUrl.contains("/viewRenovation/1"),
                "Expected to be redirected to renovation records page page, got: " + redirectUrl);
    }

    @And("I enter a blank record name")
    public void iEnterABlankRecordName() {
        renovationName = "";
        renovationDescription = "A renovation description";
        roomNames.clear();
    }

    @Then("Then an error message tells me the name cannot be empty")
    public void thenAnErrorMessageTellsMeTheNameCannotBeBlank() {
        HashMap<String, String> errors = getHashMapErrorsFromModel();

        Assertions.assertTrue(errors.containsKey("name"));
        Assertions.assertEquals("Renovation record name cannot be empty", errors.get("name"),
                "Expected an error message : '" + "Renovation record name cannot be empty" + "'");
    }

    @And("I enter a invalid renovation name")
    public void iEnterAInvalidRenovationName() {
        renovationName = "Renov@tion#$%";
        renovationDescription = "A renovation description";
        roomNames.clear();
    }

    @Then("Then an error message tells me the categories of characters that are allowed and renovation is not updated")
    public void thenAnErrorMessageTellsMeTheCategoriesOfCharactersThatAreAllowedAndRenovationIsNotUpdated() {
        HashMap<String, String> errors = getHashMapErrorsFromModel();

        Assertions.assertTrue(errors.containsKey("name"));
        Assertions.assertEquals(
                "Renovation record name must only include letters, numbers, spaces, dots, hyphens or apostrophes",
                errors.get("name"),
                "Expected an error message : '" + "Renovation record name cannot by empty" + "'");
    }

    @And("I enter a non-unique renovation name")
    public void iEnterANonUniqueRenovationName() {
        RenovationRecord duplicateRenovation = new RenovationRecord("Renovation 5",
                "Renovating the bathrooms");
        duplicateRenovation.setUser(
                userRepository.findByEmailIgnoreCase("sarahandjackthompson@gmail.com").get());
        renovationRecordRepository.save(duplicateRenovation);

        renovationName = "Renovation 5";
        renovationDescription = "A renovation description";
        roomNames.clear();
    }

    @Then("Then an error message tells me the name is not unique")
    public void thenAnErrorMessageTellsMeTheNameIsNotUnique() {
        HashMap<String, String> errors = getHashMapErrorsFromModel();

        Assertions.assertTrue(errors.containsKey("duplicate"));
        Assertions.assertTrue(errors.get("duplicate").contains("not unique"),
                "Expected an error message : '" + "Renovation record is not unique" + "'");
    }

    @And("I enter a invalid room name")
    public void iEnterAInvalidRoomName() {
        renovationName = "Renovation 1";
        renovationDescription = "A renovation description";
        roomNames.clear();
        roomNames.add("Bathrooms#$%");
    }

    @And("I enter a description longer than {int} characters")
    public void iEnterADescriptionLongerThanCharacters(int maxDescriptionLength) {
        renovationName = "Renovation 1";
        renovationDescription = "a".repeat(maxDescriptionLength + 1);
        roomNames.clear();
    }

    @Then("Then an error message tells me the the description must be {int} characters or less")
    public void thenAnErrorMessageTellsMeTheTheDescriptionMustBeCharactersOrLess(
            int maxDescriptionLength) {
        HashMap<String, String> errors = getHashMapErrorsFromModel();

        Assertions.assertEquals(
                "Renovation record description must be 512 characters or less",
                errors.get("description"),
                "Expected an error message : '"
                        + "Renovation record description must be 512 characters or less" + "'");
    }

    @When("I click cancel on the edit renovation page")
    public void iClickCancelOnTheEditRenovationPage() throws Exception {
        mvcResult = mockMvc.perform(
                get("/viewRenovation/" + renovationRecordRepository.findById(1L).get().getId()
                        .toString()).with(csrf())).andReturn();
    }

    @Then("Then I am taken back to the renovation record details page and the changes were not saved")
    public void thenIAmTakenBackToTheRenovationRecordDetailsPageAndTheChangesWereNotSaved() {
        String viewName = Objects.requireNonNull(mvcResult.getModelAndView()).getViewName();
        Assertions.assertEquals("record-view", viewName);
        Assertions.assertEquals(renovationRecordRepository.findById(1L).get().getName(),
                renovationName);
        Assertions.assertEquals(renovationRecordRepository.findById(1L).get().getDescription(),
                renovationDescription);
    }
}
