package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationDesign;
import nz.ac.canterbury.seng302.homehelper.service.RenovationDesignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class U1001a_3DRoomDesignCreationFeature {

    private final Long renovationRecordId = 1L;
    private final Long designId = 1L;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RenovationDesignService renovationDesignService;
    private Long roomId;

    @Given("I am on the design editor page")
    public void iAmOnTheDesignEditorPage() throws Exception {
        mockMvc.perform(
                        get("/renovationRecord/" + renovationRecordId + "/editDesign/" + designId))
                .andExpect(status().isOk());
    }

    @When("I select a room from the rooms drop-down menu")
    public void iSelectARoomFromTheRoomsDropDownMenu() {
        roomId = 2L;
    }

    @When("I click to save the design")
    public void iClickToSaveTheDesign() throws Exception {
        String designName = "Design";
        String designDescription = "Description";

        String jsonString = String.format(
                "{\"name\": \"%s\", \"description\": \"%s\", \"designRoomId\": \"%s\"}",
                designName, designDescription, roomId);

        MockMultipartFile jsonPart = new MockMultipartFile(
                "json",
                // This must match the key used in formData.append("json", ...) on the ts side
                "json",                    // Filename (not important here)
                "application/json",        // Content-Type
                jsonString.getBytes(StandardCharsets.UTF_8)
        );

        byte[] testGlbBytes = Files.readAllBytes(
                Paths.get("src", "test", "resources", "testGlbFiles", "test_scene.glb"));

        MockMultipartFile designGlbFile = new MockMultipartFile(
                "designGLB", // name of the field (matches formData.append in the saving logic)
                "model.glb", // original file name
                "model/gltf-binary", // content type (MIME type)
                testGlbBytes // file content
        );

        mockMvc.perform(
                        multipart("/renovationRecord/" + renovationRecordId + "/saveDesign/" + designId)
                                .file(designGlbFile)
                                .file(jsonPart)
                                .with(csrf())
                )
                .andExpect(status().isOk());
    }

    @Then("The room is applied to the design I was editing")
    public void theRoomIsAppliedToTheDesignIWasEditing() {
        RenovationDesign savedRenovationDesign = renovationDesignService.getDesignById(designId);

        String expectedRoomName = "Dining Room";
        assertEquals(expectedRoomName, savedRenovationDesign.getRelatedRoom().getName());
        assertEquals(roomId, savedRenovationDesign.getRelatedRoom().getId());
    }
}
