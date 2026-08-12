package nz.ac.canterbury.seng302.homehelper.integration.controller;

import static nz.ac.canterbury.seng302.homehelper.utils.UploadDirectory.CUSTOM_TEXTURES;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import nz.ac.canterbury.seng302.homehelper.entity.SceneTexture;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.SceneTextureRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.utils.FileUtilities;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
// this means we can do a before all that has the state of the class and doesn't have to be static
class EditDesignControllerIntegrationTest {

    private static final Path UPLOAD_DIR = Path.of("uploads/custom-textures");
    private static final Long USER_1_ID = 1L;
    private static final Long USER_2_ID = 2L;
    private static final String TEXTURE_FILE_NAME = "texture_id1.jpg";
    private static User testUser1;
    private static User testUser2;

    @Autowired
    SceneTextureRepository sceneTextureRepository;
    @Autowired
    FileUtilities fileUtilities;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;
    @MockBean
    UserService userService;
    private Long textureId;

    @BeforeAll
    void setUp() throws Exception {
        testUser1 = new User("jane@doe.co.nz", "P4$$word", "Jane", "Doe");
        testUser1.setId(USER_1_ID);
        userRepository.save(testUser1);
        testUser2 = new User("john@doe.co.nz", "P4$$word", "John", "Doe");
        testUser2.setId(USER_2_ID);
        userRepository.save(testUser2);

        Files.createDirectories(UPLOAD_DIR); // makes sure the upload dir exists
    }

    @AfterEach
    void cleanup() throws IOException {
        Files.list(UPLOAD_DIR).forEach(path -> path.toFile().delete());
        sceneTextureRepository.deleteAll();
    }

    private void createTestSceneTexture() throws IOException {
        fileUtilities.saveBytesToFile(new byte[1], CUSTOM_TEXTURES, TEXTURE_FILE_NAME);
        SceneTexture texture = new SceneTexture("texture1", testUser1,
                CUSTOM_TEXTURES.getRelativePathForDB().resolve(TEXTURE_FILE_NAME).toString());
        SceneTexture texture1 = sceneTextureRepository.save(texture);
        this.textureId = texture1.getId();
    }

    @Test
    void deleteTexture_successfullyDeletesExistingTexture() throws Exception {
        Mockito.when(userService.getLoggedUser()).thenReturn(testUser1);

        createTestSceneTexture();

        mockMvc.perform(delete("/deleteTexture/" + this.textureId)
                        .with(csrf())
                        .with(user(testUser1.getEmail()).roles("USER")))
                .andExpect(status().isOk());

        // check that texture entity is deleted
        Assertions.assertFalse(sceneTextureRepository.findById(this.textureId).isPresent());

        // check that texture file is deleted
        Assertions.assertFalse(fileUtilities.fileExists(CUSTOM_TEXTURES, TEXTURE_FILE_NAME));
    }

    @Test
    void deleteTexture_TextureDoesNotExist_ReturnsForbidden() throws Exception {
        Mockito.when(userService.getLoggedUser()).thenReturn(testUser1);

        createTestSceneTexture();

        mockMvc.perform(delete("/deleteTexture/" + 2L)
                        .with(csrf())
                        .with(user(testUser1.getEmail()).roles("USER")))
                .andExpect(status().isForbidden());

        // check that texture entity hasn't been deleted
        Assertions.assertTrue(sceneTextureRepository.findById(this.textureId).isPresent());

        // check that texture file hasn't been deleted
        Assertions.assertTrue(fileUtilities.fileExists(CUSTOM_TEXTURES, TEXTURE_FILE_NAME));
    }

    @Test
    void deleteTexture_UserDoesNotOwn_ReturnsForbidden() throws Exception {
        Mockito.when(userService.getLoggedUser()).thenReturn(testUser2);

        createTestSceneTexture();

        mockMvc.perform(delete("/deleteTexture/" + this.textureId)
                        .with(csrf())
                        .with(user(testUser2.getEmail()).roles("USER")))
                .andExpect(status().isForbidden());

        // check that texture entity hasn't been deleted
        Assertions.assertTrue(sceneTextureRepository.findById(this.textureId).isPresent());

        // check that texture file hasn't been deleted
        Assertions.assertTrue(fileUtilities.fileExists(CUSTOM_TEXTURES, TEXTURE_FILE_NAME));
    }

}
