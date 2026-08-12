package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.HashMap;
import nz.ac.canterbury.seng302.homehelper.cucumber.hooks.TestContext;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.RenovationService;
import nz.ac.canterbury.seng302.homehelper.utils.profanity.ProfanityChecker;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class U21_TagModerationFeature {

    @Autowired
    public MockMvc mockMvc;
    @Autowired
    ProfanityChecker profanityChecker;
    @Autowired
    RenovationService renovationService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    private TestContext testContext;
    private RenovationRecord record;

    @When("I type a profane word {string} in the tag field and click Add Tag")
    public void iTypeAProfaneWordInTheTagFieldAndClickAddTag(String input) throws Exception {
        Mockito.when(profanityChecker.isProfanePerspective(Mockito.anyString())).thenReturn(true);
        record = new RenovationRecord(
                userRepository.findByEmailIgnoreCase("sarahandjackthompson@gmail.com").get(),
                "name", "description");
        record.setId(1L);
        renovationService.save(record);

        testContext.httpResponse = mockMvc.perform(post("/viewRenovation/addTags/{id}", 1)
                        .with(csrf())
                        .param("tagName", input))
                .andReturn();
    }

    @Then("I receive an error message that tells me the tag doesn't meet the system language standards")
    public void iReceiveAnErrorMessageThatTellsMeTheTagDoesnTMeetTheSystemLanguageStandards() {
        int status = testContext.httpResponse.getResponse().getStatus();
        String redirectUrl = testContext.httpResponse.getResponse().getRedirectedUrl();

        Assertions.assertTrue(status >= 300 && status < 400);
        Assertions.assertTrue(redirectUrl != null && redirectUrl.contains("/viewRenovation"));

        Object rawErrors = testContext.httpResponse.getFlashMap().get("errors");

        if (!(rawErrors instanceof HashMap)) {
            throw new IllegalStateException("Expected 'errors' to be a HashMap");
        }
        HashMap<String, String> errors = (HashMap<String, String>) rawErrors;
        Assertions.assertTrue(errors.containsKey("tag"));
        Assertions.assertTrue(errors.get("tag").contains(
                "The tag entered is profane and does not follow the system language standards"));
        Assertions.assertTrue(renovationService.getRenovationRecordById(1L).getTags().isEmpty());
    }
}
