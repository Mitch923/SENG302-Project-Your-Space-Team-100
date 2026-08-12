package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import nz.ac.canterbury.seng302.homehelper.entity.SceneTexture;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.SceneTextureRepository;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class U1006b_CustomTexturesUploadingFeature {

    MockMultipartFile textureFile;
    private MvcResult httpResponse;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SceneTextureRepository sceneTextureRepository;
    @Autowired
    private UserService userService;

    @And("I have the file picker for textures open")
    public void iHaveTheFilePickerForTexturesOpen() throws IOException {
        byte[] testImageBytes = Files.readAllBytes(
                Paths.get("src", "test", "resources", "test.ImageUploadValidator.images",
                        "test.png"));

        textureFile = new MockMultipartFile(
                "file", // must match the name in the formData.append
                "texture.png",
                "image/png",
                testImageBytes
        );
    }


    @When("I select an individual texture file and click upload")
    public void iSelectAnIndividualTextureFileAndClickUpload() throws Exception {
        mockMvc.perform(
                        multipart("/uploadTexture")
                                .file(textureFile)
                                .with(csrf())
                )
                .andExpect(status().isOk());
    }

    @Then("the texture is added to my repository of custom textures")
    public void theTextureIsAddedToMyRepositoryOfCustomTextures() {
        List<SceneTexture> textures = sceneTextureRepository.findSceneTexturesByName("texture");
        Assertions.assertEquals(1, textures.size());
    }

    @When("I open the textures tab")
    public void iOpenTheTexturesTab() throws Exception {
        User loggedInUser = userService.getUserByEmail("john@example.com").get();
        for (int i = 0; i < 5; i++) {
            sceneTextureRepository.save(
                    new SceneTexture("Texture " + i, loggedInUser, "texture_path_" + i));
        }

        httpResponse = mockMvc.perform(
                        get("/renovationRecord/" + 1 + "/editDesign/" + 1))
                .andExpect(status().isOk()).andReturn();

    }

    @Then("I can see a list of textures uploaded in the past.")
    public void iCanSeeAListOfTexturesUploadedInThePast() {
        Object texturesObj = Objects.requireNonNull(
                Objects.requireNonNull(httpResponse.getModelAndView())
                        .getModelMap().getAttribute("customTextures"));

        @SuppressWarnings("unchecked")
        List<SceneTexture> textures = (List<SceneTexture>) texturesObj;

        Assertions.assertFalse(textures.isEmpty());
        Assertions.assertInstanceOf(SceneTexture.class, textures.get(0));

        Assertions.assertEquals(6, textures.size());
    }

}
