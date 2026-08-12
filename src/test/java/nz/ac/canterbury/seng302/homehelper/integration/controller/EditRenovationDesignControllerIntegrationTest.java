package nz.ac.canterbury.seng302.homehelper.integration.controller;

import static nz.ac.canterbury.seng302.homehelper.utils.UploadDirectory.CUSTOM_MODELS;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.annotation.PostConstruct;
import nz.ac.canterbury.seng302.homehelper.auth.CustomAuthenticationProvider;
import nz.ac.canterbury.seng302.homehelper.controller.EditRenovationDesignController;
import nz.ac.canterbury.seng302.homehelper.entity.SceneModel;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.SceneModelRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.SceneModelService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.utils.FileUtilities;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
class EditRenovationDesignControllerIntegrationTest {

    @Autowired
    EditRenovationDesignController editRenovationDesignController;
    @Autowired
    UserService userService;
    @Autowired
    SceneModelService sceneModelService;
    @Autowired
    SceneModelRepository sceneModelRepository;
    @Autowired
    private CustomAuthenticationProvider authenticationProvider;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser1;
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FileUtilities fileUtilities;

    @PostConstruct
    public void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(editRenovationDesignController).build();

    }

    @BeforeEach
    public void before() {
        String rawPassword = "P4$$word";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Create verified user
        User john = new User("johujkgvfdzuhirgjklhn@example.com", encodedPassword, "John", "Doe");
        userService.saveUser(john);
        userService.verifyUser(john);

        // Authenticate the user
        Authentication authRequest = new UsernamePasswordAuthenticationToken(john.getEmail(),
                rawPassword);
        Authentication authResult = authenticationProvider.authenticate(authRequest);

        SecurityContextHolder.getContext().setAuthentication(authResult);
        testUser1 = john;
    }

    @AfterEach
    public void after() {
        sceneModelRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void customModelExists_deleteModelRequest_responseOk() throws Exception {
        // Save model and dummy file
        String fileName = "test model deletion";
        fileUtilities.saveBytesToFile(new byte[1], CUSTOM_MODELS, fileName);
        SceneModel model = new SceneModel("test model", testUser1,
                CUSTOM_MODELS.getRelativePathForDB().resolve(fileName).toString(), "kjskj");
        sceneModelService.saveModelDetails(model);

        mockMvc.perform(delete("/deleteModel/" + model.getId()).with(csrf()))
                .andExpect(status().isOk());
        Assertions.assertFalse(fileUtilities.fileExists(CUSTOM_MODELS, fileName));
    }

    @Test
    void userDeletesAnotherUsersModel_responseForbidden() throws Exception {
        //      Add another user
        User jane = new User("janhjaewe@example.com", "DumbPassword", "Jane", "Doe");
        jane.setId(500L);
        userService.saveUser(jane);
        userService.verifyUser(jane);

        String fileName = "test model deletion";
        SceneModel model = new SceneModel("test model", jane,
                CUSTOM_MODELS.getRelativePathForDB().resolve(fileName).toString(), "kjskj");
        sceneModelService.saveModelDetails(model);

        mockMvc.perform(delete("/deleteModel/" + model.getId()).with(csrf()))
                .andExpect(status().isForbidden());
    }
}
