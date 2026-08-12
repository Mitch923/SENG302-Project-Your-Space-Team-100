package nz.ac.canterbury.seng302.homehelper.integration.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import jakarta.annotation.PostConstruct;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import nz.ac.canterbury.seng302.homehelper.controller.EditRenovationDesignController;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationDesign;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Room;
import nz.ac.canterbury.seng302.homehelper.entity.SceneModel;
import nz.ac.canterbury.seng302.homehelper.entity.SceneTexture;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationDesignRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RoomRepository;
import nz.ac.canterbury.seng302.homehelper.repository.SceneTextureRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.SceneModelService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.utils.FileUtilities;
import nz.ac.canterbury.seng302.homehelper.utils.UploadDirectory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
public class EditDesignControllerPartialIntegrationTest {

    private static MockMultipartFile mockPng;
    private static MockMultipartFile mockJpg;
    private static MockMultipartFile mockObjFile;
    private static MockMultipartFile mockEmptyFile;
    private static MockMultipartFile mockGlbFile;

    @Autowired
    EditRenovationDesignController editRenovationDesignController;
    @MockBean
    RenovationRecordRepository renovationRecordRepository;
    @MockBean
    RenovationDesignRepository renovationDesignRepository;
    @MockBean
    UserRepository userRepository;
    @MockBean
    UserService userService;
    @MockBean
    SceneModelService sceneModelService;
    @MockBean
    RoomRepository roomRepository;
    @MockBean
    FileUtilities fileUtilities;
    @MockBean
    private SceneTextureRepository sceneTextureRepository;

    private User testUser1;
    private MockMvc mockMvc;
    private RenovationRecord renovationRecord;
    private Room room;
    private RenovationDesign renovationDesign;

    private static Stream<Arguments> nullEquivalentRoomIds() {
        return Stream.of(
                Arguments.of("null"),
                Arguments.of("default"),
                Arguments.of((Object) null)
        );
    }

    public static Stream<Arguments> mockTextures() {
        return Stream.of(
                Arguments.of(mockPng),
                Arguments.of(mockJpg)
        );
    }

    private static byte[] smallValidJpeg() throws IOException {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, 0xFFFFFF); // white pixel

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", baos);
        return baos.toByteArray();
    }

    private static byte[] smallValidPng() throws IOException {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, 0xFF0000FF); // blue pixel

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }

    @BeforeAll
    static void beforeAll() throws IOException {
        mockPng = new MockMultipartFile(
                "file",
                "testFile.png",
                "image/png",
                smallValidPng()
        );
        mockJpg = new MockMultipartFile(
                "file",
                "testFile.jpg",
                "image/jpeg",
                smallValidJpeg()
        );

        mockObjFile = new MockMultipartFile(
                "modelGLB",
                "testFile.obj",
                "text/plain",
                "This is a test file".getBytes()
        );
        mockEmptyFile = new MockMultipartFile(
                "modelGLB",
                "testFile.obj",
                null,
                "This is a test file".getBytes()
        );
        mockGlbFile = new MockMultipartFile(
                "modelGLB",
                "testFile.jpg",
                "model/gltf-binary",
                "This is a test file".getBytes()
        );
    }

    @PostConstruct
    public void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(editRenovationDesignController).build();

    }

    @BeforeEach
    public void before() {
        // Set up User
        testUser1 = new User("jane@doe.co.nz", "P4$$word", "Jane", "Doe");
        testUser1.setId(1L);
        userService.verifyUser(testUser1);
        userRepository.save(testUser1);

        // Set up Renovation Record
        renovationRecord = new RenovationRecord("Renovation record",
                "Renovating Jack Erskine");
        renovationRecord.setId(1L);

        // Set up and add Room
        room = new Room("Room", renovationRecord);
        room.setId(1L);
        renovationRecord.addRooms(List.of(room));

        // Set up Design
        renovationDesign = new RenovationDesign("Untitled Design", "", renovationRecord);

        // Set up mocks
        Mockito.when(userService.userOwnsRecord(Mockito.any())).thenReturn(true);
        Mockito.when(renovationRecordRepository.findById(anyLong()))
                .thenReturn(Optional.of(renovationRecord));
        Mockito.when(renovationDesignRepository.getDesignById(anyLong()))
                .thenReturn(renovationDesign);
        Mockito.when(renovationDesignRepository.save(Mockito.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(roomRepository.findById(anyLong())).thenReturn(Optional.of(room));
        when(sceneTextureRepository.save(any()))
                .thenAnswer((invocation) -> {
                    SceneTexture texture = invocation.getArgument(0);
                    texture.setId(1L);
                    return texture; // returns S, which is SceneTexture
                });
    }


    @Test
    void saveDesign_designDetailsChanged_changesPersisted() throws Exception {

        String jsonString = String.format(
                "{\"name\": \"%s\", \"description\": \"%s\", \"designRoomId\": \"%s\"}",
                "Art Deco Bathroom", "this description is different now", room.getId());

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
                        multipart("/renovationRecord/" + 1L + "/saveDesign/" + 1L)
                                .file(designGlbFile)
                                .file(jsonPart)
                                .with(csrf())
                )
                .andReturn();

        ArgumentCaptor<RenovationDesign> designCaptor = ArgumentCaptor.forClass(
                RenovationDesign.class);
        verify(renovationDesignRepository, times(1)).save(designCaptor.capture());

        RenovationDesign savedRenovationDesign = designCaptor.getValue();

        assertNotNull(savedRenovationDesign);
        assertEquals("Art Deco Bathroom", savedRenovationDesign.getName());
        assertEquals("this description is different now", savedRenovationDesign.getDescription());
    }

    @Test
    void saveDesign_designHasBlankName_badRequest() throws Exception {
        mockMvc.perform(
                        post("/renovationRecord/{renovationId}/saveDesign/{designId}",
                                renovationRecord.getId(), 1L)
                                .param("designName", "")
                                .param("designDescription", "this description is different now")
                                .with(csrf()))
                .andExpect(status().is4xxClientError());

        verify(renovationDesignRepository, Mockito.never()).save(Mockito.any());

    }

    @Test
    void roomChanged_saveDesign_changesPersist() throws Exception {
        String jsonString = String.format(
                "{\"name\": \"%s\", \"description\": \"%s\", \"designRoomId\": \"%s\"}",
                renovationDesign.getName(), renovationDesign.getDescription(), room.getId());

        MockMultipartFile jsonPart = new MockMultipartFile(
                "json",
                "json",
                "application/json",
                jsonString.getBytes(StandardCharsets.UTF_8)
        );

        byte[] testGlbBytes = Files.readAllBytes(
                Paths.get("src", "test", "resources", "testGlbFiles", "test_scene.glb")
        );

        MockMultipartFile designGlbFile = new MockMultipartFile(
                "designGLB",
                "model.glb",
                "model/gltf-binary",
                testGlbBytes
        );

        mockMvc.perform(multipart("/renovationRecord/{renovationId}/saveDesign/{designId}",
                        renovationRecord.getId(), 1L)
                        .file(designGlbFile)
                        .file(jsonPart)
                        .with(csrf()))
                .andExpect(status().isOk());

        ArgumentCaptor<RenovationDesign> designCaptor = ArgumentCaptor.forClass(
                RenovationDesign.class);
        verify(renovationDesignRepository, times(1)).save(designCaptor.capture());

        RenovationDesign savedRenovationDesign = designCaptor.getValue();

        assertNotNull(savedRenovationDesign);
        assertEquals(room.getName(), savedRenovationDesign.getRelatedRoom().getName());
        assertEquals(room.getId(), savedRenovationDesign.getRelatedRoom().getId());
    }

    @ParameterizedTest
    @MethodSource("nullEquivalentRoomIds")
    void roomIdNull_saveDesign_noRoomInDesign(String roomId) throws Exception {
        renovationDesign.setRelatedRoom(room);

        String jsonString = String.format(
                "{\"name\": \"%s\", \"description\": \"%s\", \"designRoomId\": \"%s\"}",
                renovationDesign.getName(), renovationDesign.getDescription(), roomId);

        MockMultipartFile jsonPart = new MockMultipartFile(
                "json",
                "json",
                "application/json",
                jsonString.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/renovationRecord/{renovationId}/saveDesign/{designId}",
                        renovationRecord.getId(), 1L)
                        .file(jsonPart)
                        .with(csrf()))
                .andExpect(status().isOk());

        ArgumentCaptor<RenovationDesign> designCaptor = ArgumentCaptor.forClass(
                RenovationDesign.class);
        verify(renovationDesignRepository, times(1)).save(designCaptor.capture());
        verify(roomRepository, Mockito.never()).findById(anyLong());

        RenovationDesign savedRenovationDesign = designCaptor.getValue();

        assertNotNull(savedRenovationDesign);
        assertNull(savedRenovationDesign.getRelatedRoom());
    }

    @Test
    public void saveDesign_designNameTooLong_badRequest() throws Exception {
        mockMvc.perform(
                        post("/renovationRecord/{renovationId}/saveDesign/{designId}",
                                renovationRecord.getId(), 1L)
                                .param("designName", "a".repeat(256))
                                .param("designDescription", "Descriptive description.")
                                .with(csrf()))
                .andExpect(status().is4xxClientError());
        verify(renovationDesignRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void saveDesign_designDescriptionTooLong_badRequest() throws Exception {
        mockMvc.perform(
                        post("/renovationRecord/{renovationId}/saveDesign/{designId}",
                                renovationRecord.getId(), 1L)
                                .param("designName", "Design")
                                .param("designDescription", "d".repeat(513))
                                .with(csrf()))
                .andExpect(status().is4xxClientError());
        verify(renovationDesignRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void validModel_uploadModel_ok() throws Exception {
        long userId = testUser1.getId();
        String displayName = "Test Model";

        when(userService.getLoggedUser()).thenReturn(testUser1);
        when(userService.getUserById(userId)).thenReturn(Optional.of(testUser1));

        SceneModel mockSceneModel = new SceneModel(displayName, testUser1, "", "");
        mockSceneModel.setId(123L);

        doAnswer(invocation -> {
            SceneModel model = invocation.getArgument(0);
            model.setId(123L);
            return null;
        }).when(sceneModelService).saveModelDetails(any(SceneModel.class));

        doNothing().when(sceneModelService).saveModelData(eq(123L), any());

        doNothing().when(sceneModelService).saveModelData(eq(123L), any());

        mockMvc.perform(multipart("/upload/model/{userId}", userId) // include path var
                        .file(mockGlbFile)                           // modelGLB file
                        .param("displayName", displayName)           // request param
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }


    @Test
    void invalidModel_uploadObj_shouldReturnContentTypeError() throws Exception {
        long userId = testUser1.getId();
        String displayName = "Test Model";

        when(userService.getLoggedUser()).thenReturn(testUser1);
        when(userService.getUserById(userId)).thenReturn(Optional.of(testUser1));

        String response = mockMvc.perform(multipart("/upload/model/{userId}", userId)
                        .file(mockObjFile)
                        .param("displayName", displayName)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is4xxClientError())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals("Model file content type must be a gltf-binary", response);
    }

    @Test
    void invalidModel_uploadContentTypeEmpty_shouldReturnContentTypeError() throws Exception {
        long userId = testUser1.getId();
        String displayName = "Test Model";

        when(userService.getLoggedUser()).thenReturn(testUser1);
        when(userService.getUserById(userId)).thenReturn(Optional.of(testUser1));

        String response = mockMvc.perform(multipart("/upload/model/{userId}", userId)
                        .file(mockEmptyFile)
                        .param("displayName", displayName)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is4xxClientError())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals("Model file content type cannot be empty", response);
    }


    /* This test is not the most useful, but I have kept regardless */
    @Test
    void getPublicModelIds_returnsListOfIds() throws Exception {
        SceneModel model1 = new SceneModel("Model 1", testUser1, "", "");
        model1.setId(101L);
        SceneModel model2 = new SceneModel("Model 2", testUser1, "", "");
        model2.setId(102L);

        when(sceneModelService.getPublicModels()).thenReturn(List.of(model1, model2));

        String responseContent = mockMvc.perform(get("/ids/model/public")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertTrue(responseContent.contains("101"));
        assertTrue(responseContent.contains("102"));
    }

    @Test
    void getModelFragment_existingModel_returnsFragment() throws Exception {
        SceneModel model1 = new SceneModel("Test Model", testUser1, "/file/path", "/image/path");
        model1.setId(42L);

        when(sceneModelService.findById(42L)).thenReturn(model1);

        mockMvc.perform(get("/fragment/model/{id}", 42L))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/modelCard :: modelCard"))
                .andExpect(model().attribute("name", "Test Model"))
                .andExpect(model().attribute("id", 42L))
                .andExpect(model().attribute("imagePath", "/image/path"))
                .andExpect(model().attribute("filePath", "/file/path"))
                .andExpect(model().attribute("type", "model"));
    }

    @Test
    void getModelFragment_nonExistingModel_returnsNotFound() throws Exception {
        when(sceneModelService.findById(999L)).thenReturn(null);

        mockMvc.perform(get("/fragment/model/{id}", 999L))
                .andExpect(status().isNotFound());
    }


    @ParameterizedTest
    @MethodSource("mockTextures")
    public void validTexture_uploadTexture_ok(MockMultipartFile mockTexture) throws Exception {
        mockMvc.perform(multipart("/uploadTexture")
                        .file(mockTexture))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @MethodSource("mockTextures")
    public void validTexture_uploadTexture_passesTextureToFileUtilities(
            MockMultipartFile mockTexture) throws Exception {
        mockMvc.perform(multipart("/uploadTexture")
                .file(mockTexture));

        ArgumentCaptor<MultipartFile> fileCaptor = ArgumentCaptor.forClass(MultipartFile.class);
        ArgumentCaptor<UploadDirectory> dirCaptor = ArgumentCaptor.forClass(UploadDirectory.class);
        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);

        verify(fileUtilities).saveMultipartFile(fileCaptor.capture(), dirCaptor.capture(),
                nameCaptor.capture());

        MultipartFile capturedFile = fileCaptor.getValue();
        UploadDirectory capturedDir = dirCaptor.getValue();

        assertEquals(mockTexture.getBytes(), capturedFile.getBytes());
        assertEquals(Paths.get("test-uploads/textures/custom"), capturedDir.getRelativePath());
    }

    @ParameterizedTest
    @MethodSource("mockTextures")
    public void validTexture_uploadTexture_passesTextureToRepository(
            MockMultipartFile mockTexture) throws Exception {
        when(userService.getLoggedUser()).thenReturn(testUser1);

        mockMvc.perform(multipart("/uploadTexture")
                .file(mockTexture));

        ArgumentCaptor<SceneTexture> textureCaptor = ArgumentCaptor.forClass(SceneTexture.class);

        verify(sceneTextureRepository, times(2)).save(textureCaptor.capture());

        SceneTexture texture = textureCaptor.getValue();
        String expectedTextureName = mockTexture.getOriginalFilename().substring(0,
                mockTexture.getOriginalFilename().length() - 4);

        assertEquals(expectedTextureName, texture.getName());
        assertEquals(Paths.get(
                "uploads/textures/custom/texture_id1" + FileUtilities.getFileExtensionFromName(
                        mockTexture.getOriginalFilename())).toString(), texture.getTexturePath());
        assertEquals(testUser1.getId(), texture.getUser().getId());
    }
}
