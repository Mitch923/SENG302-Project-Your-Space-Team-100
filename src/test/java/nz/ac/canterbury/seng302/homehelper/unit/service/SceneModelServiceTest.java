package nz.ac.canterbury.seng302.homehelper.unit.service;

import static org.mockito.Mockito.when;

import java.util.Optional;
import nz.ac.canterbury.seng302.homehelper.entity.SceneModel;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.SceneModelRepository;
import nz.ac.canterbury.seng302.homehelper.service.SceneModelService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.utils.FileUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SceneModelServiceTest {

    private SceneModelService sceneModelService;
    @Mock
    private UserService userService;
    @Mock
    private SceneModelRepository sceneModelRepository;
    @Mock
    private FileUtilities fileUtil;
    private User testUser1;

    @BeforeEach
    public void setUp() {
        this.sceneModelService = new SceneModelService(userService, sceneModelRepository, fileUtil);
        testUser1 = new User("john@example.com", "P4$$word", "John", "Doe");
    }

    @Test
    void modelExists_deleteModel_returnTrue() {
        SceneModel model = new SceneModel("test model", testUser1, "", "kjskj");
        model.setId(1L);
        when(sceneModelRepository.findById(Mockito.any())).thenReturn(Optional.of(model));

        boolean result = sceneModelService.deleteModel(1L);
        Assertions.assertTrue(result);
    }
}
