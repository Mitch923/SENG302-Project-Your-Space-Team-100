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
import java.util.HashMap;
import java.util.List;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import nz.ac.canterbury.seng302.homehelper.repository.TagRepository;
import nz.ac.canterbury.seng302.homehelper.service.RenovationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "OptionalGetWithoutIsPresent",
        "unchecked"})
public class U20_AddTagFeature {

    @Autowired
    public MockMvc mockMvc;

    @Autowired
    private RenovationService renovationRecordService;

    @Autowired
    private TagRepository tagRepository;

    private ResultActions resultActions;

    private RenovationRecord renovationRecord;

    @Given("I want to add a tag to my renovation record")
    public void iWantToAddATagToMyRenovationRecord() throws Exception {
        renovationRecord = renovationRecordService.getRenovationRecordById(50L);

        mockMvc.perform(get("/viewRenovation/" + renovationRecord.getId())
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Given("There are already five tags associated to that renovation record")
    public void thereAreAlreadyFiveTagsAssociatedToRenovationRecord() throws Exception {
        Tag tag1 = tagRepository.save(new Tag("tag1"));
        Tag tag2 = tagRepository.save(new Tag("tag2"));
        Tag tag3 = tagRepository.save(new Tag("tag3"));
        Tag tag4 = tagRepository.save(new Tag("tag4"));
        Tag tag5 = tagRepository.save(new Tag("tag5"));

        renovationRecord.addTag(tag1);
        renovationRecord.addTag(tag2);
        renovationRecord.addTag(tag3);
        renovationRecord.addTag(tag4);
        renovationRecord.addTag(tag5);

        renovationRecordService.save(renovationRecord);

    }

    @When("I input a tag {string} of only special characters or numbers")
    public void IInputATagOfOnlySpecialCharactersOfOnlySpecialCharactersAndNumbers(String tagName)
            throws Exception {
        resultActions = mockMvc.perform(post("/viewRenovation/addTags/" + renovationRecord.getId())
                        .param("tagName", tagName)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @When("I try to add a tag to the renovation record")
    public void iTryToAddATagToTheRenovationRecord() throws Exception {
        resultActions = mockMvc.perform(post("/viewRenovation/addTags/" + renovationRecord.getId())
                        .param("tagName", "tag6")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Then("I see an error message telling me that {string}")
    public void iSeeAnErrorMessageTellingMeThat(String errorMessage) throws Exception {
        HashMap<String, String> errors = (HashMap<String, String>) resultActions.andReturn()
                .getFlashMap().get("errors");

        assertTrue(errors.containsKey("tag"));
        assertEquals(errorMessage, errors.get("tag"));
    }

    @Then("The tag {string} is not added to the renovation record")
    public void theTagIsNotAddedToTheRenovationRecord(String tagName) throws Exception {
        renovationRecord = renovationRecordService.getRenovationRecordById(
                renovationRecord.getId());

        List<String> tagNames = renovationRecord.getTags().stream().map(Tag::getName).toList();
        assertFalse(tagNames.contains(tagName));
    }

    @Then("The tag is not added")
    public void theTagIsNotAdded() throws Exception {
        renovationRecord = renovationRecordService.getRenovationRecordById(
                renovationRecord.getId());

        List<String> tagNames = renovationRecord.getTags().stream().map(Tag::getName).toList();
        assertFalse(tagNames.contains("tag6"));
    }
}
