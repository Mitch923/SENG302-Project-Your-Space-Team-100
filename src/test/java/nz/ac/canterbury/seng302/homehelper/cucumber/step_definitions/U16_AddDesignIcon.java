package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationDesign;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationDesignRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.service.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class U16_AddDesignIcon {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private RenovationService renovationService;

    @Autowired
    private UserService userService;

    private RenovationDesign testRenovationDesign;

    private User john;

    @Autowired
    private RenovationDesignRepository renovationDesignRepository;

    private MvcResult mvcResult;

    @PostConstruct
    public void before() throws IOException {
        List<User> users = userService.createDefaultUsers(0);
        john = users.get(0);
        renovationService.createDefaultRenovations(users);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                "john@example.com", "P4$$word");
        Authentication authenticated = authenticationProvider.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(authenticated);
        assertNotNull(userService.getLoggedUser());

        this.testRenovationDesign = renovationRecordRepository.findAllByUser(john).get(0)
                .getDesignsForRenovation().get(0);
    }

    @Given("I can see the available system icons")
    public void i_can_see_the_available_system_icons() {
        // Do nothing
    }

    @When("I select an icon from the available systems icons")
    public void i_select_an_icon_from_the_available_systems_icons() throws Exception {
        String jsonBody = "{\"iconName\": \"buildingGear\"}";
        mvcResult = mockMvc.perform(
                MockMvcRequestBuilders.post(
                                "/viewRenovation/updateIcon/" + testRenovationDesign.getId())
                        .with(csrf())
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andReturn();
    }

    @Then("the chosen icon is displayed together with the design everywhere on the system")
    public void the_chosen_icon_is_displayed_together_with_the_design_everywhere_on_the_system() {
        assertEquals(200, mvcResult.getResponse().getStatus());
        testRenovationDesign = renovationDesignRepository.getDesignById(
                testRenovationDesign.getId());
        assertEquals("buildingGear", testRenovationDesign.getIconName());
    }

    @Given("I am viewing Renovation Record Designs")
    public void iAmViewingRenovationRecordDesigns() {
        // Shouldn't need to do anything here
    }
}

