package nz.ac.canterbury.seng302.homehelper.integration.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import nz.ac.canterbury.seng302.homehelper.entity.SceneModel;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.SceneModelRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.SceneModelService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

@SpringBootTest
public class SceneModelServiceIntegrationTests {

    @Autowired
    SceneModelRepository sceneModelRepository;

    @Autowired
    SceneModelService sceneModelService;

    @Autowired
    UserRepository userRepository;

    @SpyBean
    UserService userService;

    @AfterEach
    public void tearDown() {
        sceneModelRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void saveSceneModel() {
        User user = userRepository.save(new User("john@example.com", "password", "John", "Doe"));
        SceneModel sceneModel = new SceneModel("Chair", user, "modelPath", "modelImagePath");

        sceneModelService.saveModelDetails(sceneModel);

        SceneModel savedModel = sceneModelRepository.getByName("Chair");
        assertEquals(savedModel.getName(), sceneModel.getName());
        assertEquals(savedModel.getModelPath(), sceneModel.getModelPath());

    }

    @Test
    void userHasModels_getModelsForUser_onlyUsersModelsReturned() {
        User user = userRepository.save(new User("john@example.com", "password", "John", "Doe"));
        List<String> modelNames = List.of("Chair", "Chicken Joe", "Really Cool Chair",
                "Really Cool Chicken");
        List<String> modelPaths = List.of("Chair_path", "Chicken_Joe_path",
                "Really_Cool_Chair_path", "Really_Cool_Chicken_path");
        List<SceneModel> sceneModels = new ArrayList<>();
        for (int i = 0; i < modelNames.size(); i++) {
            if (modelNames.get(i).equals("Chair")) {
                sceneModels.add(new SceneModel(modelNames.get(i), null, modelPaths.get(i),
                        "modelImagePath"));

            } else {
                sceneModels.add(new SceneModel(modelNames.get(i), user, modelPaths.get(i),
                        "modelImagePath"));
            }
            sceneModelService.saveModelDetails(sceneModels.get(i));
        }

        Mockito.doReturn(user).when(userService).getLoggedUser();

        List<SceneModel> retrievedModels = sceneModelService.getSceneModelsForUser();
        assertEquals(3, retrievedModels.size());
        for (SceneModel retrievedModel : retrievedModels) {
            assertTrue(sceneModels.contains(retrievedModel));
        }
    }

    @Test
    void userNoHasModels_getModelsForUser_onlyPublicModelsReturned() {
        User user = userRepository.save(new User("john@example.com", "password", "John", "Doe"));
        User otherUser = userRepository.save(
                new User("jane@example.com", "password", "Jane", "Doe"));
        List<String> modelNames = List.of("Chair", "Chicken Joe", "Really Cool Chair",
                "Really Cool Chicken");
        List<String> modelPaths = List.of("Chair_path", "Chicken_Joe_path",
                "Really_Cool_Chair_path", "Really_Cool_Chicken_path");
        List<SceneModel> sceneModels = new ArrayList<>();
        for (int i = 0; i < modelNames.size(); i++) {
            if (modelNames.get(i).equals("Chair")) {
                sceneModels.add(new SceneModel(modelNames.get(i), null, modelPaths.get(i),
                        "modelImagePath"));

            } else {
                sceneModels.add(new SceneModel(modelNames.get(i), user, modelPaths.get(i),
                        "modelImagePath"));
            }
            sceneModelService.saveModelDetails(sceneModels.get(i));
        }

        Mockito.doReturn(otherUser).when(userService).getLoggedUser();

        List<SceneModel> retrievedModels = sceneModelService.getSceneModelsForUser();
        assertEquals(0, retrievedModels.size());
        for (SceneModel retrievedModel : retrievedModels) {
            assertTrue(sceneModels.contains(retrievedModel));
        }
    }

    @Test
    @Transactional
    void validCustomModel_saveModelData_customModelSaved() throws IOException {
        User user = new User("john@example.com", "password", "John", "Doe");
        userRepository.save(user);

        SceneModel testModel = new SceneModel("Chair", user, "modelPath", "modelImagePath");
        sceneModelRepository.save(testModel);

        Path modelPath = Paths.get("uploads", "models", "custom",
                "model_id" + testModel.getId() + ".glb");

        byte[] testData = new byte[]{0x41};

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            Path fakePath = Path.of("fakePath");
            filesMock.when(() -> Files.write(any(Path.class), any(byte[].class)))
                    .thenReturn(fakePath);

            sceneModelService.saveModelData(testModel.getId(), testData);

            // check the file is actually written to the filesystem
            filesMock.verify(() -> Files.write(any(Path.class), eq(testData)), times(1));
        }

        SceneModel retrieved = sceneModelRepository.findById(testModel.getId()).orElseThrow();
        assertEquals(modelPath.toString(), Paths.get(retrieved.getModelPath()).toString());
        assertEquals(testModel.getName(), retrieved.getName());
    }
}
