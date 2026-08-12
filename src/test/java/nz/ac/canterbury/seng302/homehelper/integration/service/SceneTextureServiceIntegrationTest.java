package nz.ac.canterbury.seng302.homehelper.integration.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import nz.ac.canterbury.seng302.homehelper.entity.SceneTexture;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.SceneTextureRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.SceneTextureService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.utils.FileUtilities;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
public class SceneTextureServiceIntegrationTest {

    private static MultipartFile mockPng;
    private static MultipartFile mockJpg;
    private static MultipartFile mockSvg;

    @Autowired
    SceneTextureRepository sceneTextureRepository;

    @Autowired
    SceneTextureService sceneTextureService;

    @Autowired
    UserRepository userRepository;

    @SpyBean
    UserService userService;

    @SpyBean
    FileUtilities fileUtilities;


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
        File mockPngFile = File.createTempFile("testFile", ".png");
        File mockJpgFile = File.createTempFile("testFile", ".jpg");
        mockPngFile.deleteOnExit();
        mockJpgFile.deleteOnExit();
        Path mockPngPath = Paths.get(mockPngFile.getAbsolutePath());
        Path mockJpgPath = Paths.get(mockJpgFile.getAbsolutePath());
        Files.write(mockPngPath, smallValidPng());
        Files.write(mockJpgPath, smallValidJpeg());
        mockPng = FileUtilities.generateMultipartFileFromFile(mockPngFile);
        mockJpg = FileUtilities.generateMultipartFileFromFile(mockJpgFile);
    }

    public static Stream<Arguments> mockTextures() {
        return Stream.of(
                Arguments.of(mockPng),
                Arguments.of(mockJpg)
        );
    }

    @AfterEach
    public void tearDown() {
        sceneTextureRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void publicTexturesExist_getPublicTextures_publicTexturesReturned() {
        User user = userRepository.save(new User("john@example.com", "password", "John", "Doe"));
        User otherUser = userRepository.save(
                new User("jane@example.com", "password", "Jane", "Doe"));
        List<String> publicTextureNames = List.of("cobblestone", "concrete", "tiles", "wood");
        List<String> userTexturesNames = List.of("plastic", "fur", "wool", "ice");
        List<String> otherUserTextureNames = List.of("vinyl", "grass", "dirt", "glass");

        // Create public textures
        List<SceneTexture> publicTextures = new ArrayList<>();
        for (int i = 0; i < publicTextureNames.size(); i++) {
            publicTextures.add(new SceneTexture(publicTextureNames.get(i), null,
                    "uploads/default-textures/" + publicTextureNames.get(i)));
            sceneTextureService.saveSceneTexture(publicTextures.get(i));
        }

        // Create user's textures
        List<SceneTexture> userTextures = new ArrayList<>();
        for (int i = 0; i < userTexturesNames.size(); i++) {
            userTextures.add(new SceneTexture(userTexturesNames.get(i), user,
                    "uploads/default-textures/" + userTexturesNames.get(i)));
            sceneTextureService.saveSceneTexture(userTextures.get(i));
        }

        // Create other user's textures
        List<SceneTexture> otherUserTextures = new ArrayList<>();
        for (int i = 0; i < otherUserTextureNames.size(); i++) {
            otherUserTextures.add(new SceneTexture(otherUserTextureNames.get(i), otherUser,
                    "uploads/default-textures/" + otherUserTextureNames.get(i)));
            sceneTextureService.saveSceneTexture(otherUserTextures.get(i));
        }

        Mockito.doReturn(user).when(userService).getLoggedUser();

        List<SceneTexture> retrievedTextures = sceneTextureService.getPublicTextures();
        assertEquals(publicTextures.size(), retrievedTextures.size());
        for (SceneTexture texture : retrievedTextures) {
            assertTrue(publicTextures.contains(texture));
        }
    }

    @Test
    void userNoTextures_getTexturesForUser_noTexturesReturned() {
        User user = userRepository.save(new User("john@example.com", "password", "John", "Doe"));
        User otherUser = userRepository.save(
                new User("jane@example.com", "password", "Jane", "Doe"));
        List<String> publicTextureNames = List.of("cobblestone", "concrete", "tiles", "wood");
        List<String> otherUserTextureNames = List.of("vinyl", "grass", "dirt", "glass");

        // Create public textures
        List<SceneTexture> publicTextures = new ArrayList<>();
        for (int i = 0; i < publicTextureNames.size(); i++) {
            publicTextures.add(new SceneTexture(publicTextureNames.get(i), null,
                    "uploads/default-textures/" + publicTextureNames.get(i)));
            sceneTextureService.saveSceneTexture(publicTextures.get(i));
        }

        // Create other user's textures
        List<SceneTexture> otherUserTextures = new ArrayList<>();
        for (int i = 0; i < otherUserTextureNames.size(); i++) {
            otherUserTextures.add(new SceneTexture(otherUserTextureNames.get(i), otherUser,
                    "uploads/default-textures/" + otherUserTextureNames.get(i)));
            sceneTextureService.saveSceneTexture(otherUserTextures.get(i));
        }

        Mockito.doReturn(user).when(userService).getLoggedUser();

        List<SceneTexture> retrievedTextures = sceneTextureService.getUsersCustomTextures();
        assertEquals(0, retrievedTextures.size());
    }

    @Test
    void userHasTextures_getTexturesForUser_userTexturesReturned() {
        User user = userRepository.save(new User("john@example.com", "password", "John", "Doe"));
        User otherUser = userRepository.save(
                new User("jane@example.com", "password", "Jane", "Doe"));
        List<String> publicTextureNames = List.of("cobblestone", "concrete", "tiles", "wood");
        List<String> userTexturesNames = List.of("plastic", "fur", "wool", "ice");
        List<String> otherUserTextureNames = List.of("vinyl", "grass", "dirt", "glass");

        // Create public textures
        List<SceneTexture> publicTextures = new ArrayList<>();
        for (int i = 0; i < publicTextureNames.size(); i++) {
            publicTextures.add(new SceneTexture(publicTextureNames.get(i), null,
                    "uploads/default-textures/" + publicTextureNames.get(i)));
            sceneTextureService.saveSceneTexture(publicTextures.get(i));
        }

        // Create user's textures
        List<SceneTexture> userTextures = new ArrayList<>();
        for (int i = 0; i < userTexturesNames.size(); i++) {
            userTextures.add(new SceneTexture(userTexturesNames.get(i), user,
                    "uploads/default-textures/" + userTexturesNames.get(i)));
            sceneTextureService.saveSceneTexture(userTextures.get(i));
        }

        // Create other user's textures
        List<SceneTexture> otherUserTextures = new ArrayList<>();
        for (int i = 0; i < otherUserTextureNames.size(); i++) {
            otherUserTextures.add(new SceneTexture(otherUserTextureNames.get(i), otherUser,
                    "uploads/default-textures/" + otherUserTextureNames.get(i)));
            sceneTextureService.saveSceneTexture(otherUserTextures.get(i));
        }

        Mockito.doReturn(user).when(userService).getLoggedUser();

        List<SceneTexture> retrievedTextures = sceneTextureService.getUsersCustomTextures();
        assertEquals(userTextures.size(), retrievedTextures.size());
        for (SceneTexture texture : retrievedTextures) {
            assertTrue(userTextures.contains(texture));
        }
    }

    @ParameterizedTest
    @MethodSource("mockTextures")
    public void validTextureUploaded_uploadTexture_savesTextureToUsersTextures(
            MultipartFile mockTexture)
            throws IOException {
        Map<String, String> errors = new HashMap<>();
        User user = userRepository.save(new User("john@example.com", "password", "John", "Doe"));
        Mockito.doReturn(user).when(userService).getLoggedUser();

        sceneTextureService.uploadTexture(errors, mockTexture);

        List<SceneTexture> usersTextures = sceneTextureRepository.findAllByUser(user);
        String expectedTextureName = mockTexture.getOriginalFilename().substring(0,
                mockTexture.getOriginalFilename().length() - 4);
        SceneTexture savedTexture = usersTextures.getFirst();
        assertEquals(expectedTextureName, savedTexture.getName());
        assertEquals(Paths.get(
                                "uploads/textures/custom/texture_id" + savedTexture.getId()
                                        + FileUtilities.getFileExtensionFromName(mockTexture.getOriginalFilename()))
                        .toString(),
                savedTexture.getTexturePath());
    }

    @ParameterizedTest
    @MethodSource("mockTextures")
    public void validTextureUploaded_uploadTexture_savesTexturesToFileSystem(
            MultipartFile mockTexture) throws IOException {
        Map<String, String> errors = new HashMap<>();
        User user = userRepository.save(new User("john@example.com", "password", "John", "Doe"));
        Mockito.doReturn(user).when(userService).getLoggedUser();

        sceneTextureService.uploadTexture(errors, mockTexture);

        List<SceneTexture> usersTextures = sceneTextureRepository.findAllByUser(user);
        SceneTexture savedTexture = usersTextures.getFirst();
        String expectedFilePath = "test-uploads/textures/custom/texture_id" + savedTexture.getId()
                + FileUtilities.getFileExtensionFromName(mockTexture.getOriginalFilename());
        Path path = Paths.get(expectedFilePath);
        assertTrue(Files.exists(path));
        assertArrayEquals(Files.readAllBytes(path), mockTexture.getBytes());
    }

    @ParameterizedTest
    @MethodSource("mockTextures")
    public void uploadsFolderDoesntExist_uploadTexture_noExceptions(MultipartFile mockTexture)
            throws IOException {
        Map<String, String> errors = new HashMap<>();
        User user = userRepository.save(new User("john@example.com", "password", "John", "Doe"));
        Mockito.doReturn(user).when(userService).getLoggedUser();

        Path folder = Paths.get("test-uploads");
        if (Files.exists(folder)) {
            Files.walk(folder)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }

        assertDoesNotThrow(() -> sceneTextureService.uploadTexture(errors, mockTexture));
    }
}
