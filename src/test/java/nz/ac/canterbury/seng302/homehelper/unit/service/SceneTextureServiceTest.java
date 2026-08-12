package nz.ac.canterbury.seng302.homehelper.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import nz.ac.canterbury.seng302.homehelper.entity.SceneTexture;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.SceneTextureRepository;
import nz.ac.canterbury.seng302.homehelper.service.SceneTextureService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.utils.FileUtilities;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

public class SceneTextureServiceTest {

    private static MultipartFile mockPng;
    private static MultipartFile mockJpg;

    private static MultipartFile invalidPngType;
    private static MultipartFile invalidJpgType;
    private static MultipartFile invalidSvgType;

    private static MultipartFile invalidPngContent;
    private static MultipartFile invalidJpgContent;
    private static MultipartFile invalidSvgContent;

    private static MultipartFile tooLargePng;
    private static MultipartFile tooLargeJpg;
    private static MultipartFile tooLargeSvg;
    private SceneTextureService sceneTextureService;
    @Mock
    private UserService userService;
    @Mock
    private FileUtilities fileService;
    @Mock
    private SceneTextureRepository sceneTextureRepository;

    public static Stream<Arguments> mockTextures() {
        return Stream.of(
                Arguments.of(mockPng),
                Arguments.of(mockJpg)
        );
    }

    public static Stream<Arguments> mockInvalidTextures() {
        return Stream.of(
                Arguments.of(invalidPngType, "Texture file must be of type png or jpg"),
                Arguments.of(invalidJpgType, "Texture file must be of type png or jpg"),
                Arguments.of(invalidSvgType, "Texture file must be of type png or jpg"),
                Arguments.of(invalidPngContent, "Texture file must be of type png or jpg"),
                Arguments.of(invalidJpgContent, "Texture file must be of type png or jpg"),
                Arguments.of(invalidSvgContent, "Texture file must be of type png or jpg"),
                Arguments.of(tooLargePng,
                        "Texture file must be of type png or jpg. File upload must be less than 10MB"),
                Arguments.of(tooLargeJpg,
                        "Texture file must be of type png or jpg. File upload must be less than 10MB"),
                Arguments.of(tooLargeSvg,
                        "Texture file must be of type png or jpg. File upload must be less than 10MB")
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

    private static byte[] smallInvalidImage() {
        String svg = """
                <svg xmlns="http://www.w3.org/2000/svg" width="1" height="1">
                    <rect width="1" height="1" fill="red"/>
                </svg>
                """;
        return svg.getBytes(StandardCharsets.UTF_8);
    }

    private static MultipartFile createMockFile(String fileName, String suffix, byte[] contents)
            throws IOException {
        File mockFile = File.createTempFile(fileName, suffix);
        mockFile.deleteOnExit();
        Path mockFilePath = Paths.get(mockFile.getAbsolutePath());
        Files.write(mockFilePath, contents);
        return FileUtilities.generateMultipartFileFromFile(mockFile);
    }

    @BeforeAll
    static void beforeAll() throws IOException {
        mockPng = createMockFile("testFile", ".png", smallValidPng());
        mockJpg = createMockFile("testFile", ".jpg", smallValidJpeg());
        invalidPngType = createMockFile("testFile", ".xml", smallInvalidImage());
        invalidJpgType = createMockFile("testFile", ".html", smallInvalidImage());
        invalidSvgType = createMockFile("testFile", ".svg", smallInvalidImage());

        invalidPngContent = createMockFile("testFile", ".png", new byte[]{0x1});
        invalidJpgContent = createMockFile("testFile", ".jpg", new byte[]{0x2});
        invalidSvgContent = createMockFile("testFile", ".svg", new byte[]{0x3});

        tooLargePng = createMockFile("testFile", ".png", new byte[10 * 1024 * 1024 + 1]);
        tooLargeJpg = createMockFile("testFile", ".jpg", new byte[10 * 1024 * 1024 + 1]);
        tooLargeSvg = createMockFile("testFile", ".svg", new byte[10 * 1024 * 1024 + 1]);
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sceneTextureService = new SceneTextureService(sceneTextureRepository, userService,
                fileService);

        when(sceneTextureRepository.save(any()))
                .thenAnswer((invocation) -> {
                    SceneTexture texture = invocation.getArgument(0);
                    texture.setId(1L);
                    return texture; // returns S, which is SceneTexture
                });

        User mockUser = new User("jane@example.com", "P4$$word", "Jane", "Doe");
        mockUser.setId(1L);
        when(userService.getLoggedUser()).thenReturn(mockUser);
    }

    @ParameterizedTest
    @MethodSource("mockTextures")
    public void validTextureUploaded_uploadTexture_savesTextureToRepository(
            MultipartFile mockTexture)
            throws IOException {
        Map<String, String> errors = new HashMap<>();

        sceneTextureService.uploadTexture(errors, mockTexture);
        ArgumentCaptor<SceneTexture> captor = ArgumentCaptor.forClass(SceneTexture.class);
        verify(sceneTextureRepository, times(2)).save(captor.capture());
        SceneTexture savedTexture = captor.getValue();

        String expectedTextureName = mockTexture.getOriginalFilename().substring(0,
                mockTexture.getOriginalFilename().length() - 4);
        assertEquals(expectedTextureName, savedTexture.getName());
    }

    @ParameterizedTest
    @MethodSource("mockTextures")
    public void validTextureUploaded_uploadTexture_assignsCorrectUserId(MultipartFile mockTexture)
            throws IOException {
        Map<String, String> errors = new HashMap<>();

        sceneTextureService.uploadTexture(errors, mockTexture);
        ArgumentCaptor<SceneTexture> captor = ArgumentCaptor.forClass(SceneTexture.class);
        verify(sceneTextureRepository, times(2)).save(captor.capture());
        SceneTexture savedTexture = captor.getValue();

        assertEquals(1L, savedTexture.getId());
    }

    @ParameterizedTest
    @MethodSource("mockTextures")
    public void validTextureUploaded_uploadTexture_createsCorrectTexturePath(
            MultipartFile mockTexture)
            throws IOException {
        Map<String, String> errors = new HashMap<>();

        sceneTextureService.uploadTexture(errors, mockTexture);
        ArgumentCaptor<SceneTexture> captor = ArgumentCaptor.forClass(SceneTexture.class);
        verify(sceneTextureRepository, times(2)).save(captor.capture());
        SceneTexture savedTexture = captor.getValue();

        assertEquals(Paths.get(
                        "uploads/textures/custom/texture_id1" + FileUtilities.getFileExtensionFromName(
                                mockTexture.getOriginalFilename())).toString(),
                Paths.get(savedTexture.getTexturePath()).toString());
    }

    @ParameterizedTest
    @MethodSource("mockInvalidTextures")
    public void invalidTextureUploaded_uploadTexture_rejectsInvalidTexture(
            MultipartFile mockInvalidTexture, String expectedErrorMessage
    ) throws IOException {
        Map<String, String> errors = new HashMap<>();

        sceneTextureService.uploadTexture(errors, mockInvalidTexture);
        ArgumentCaptor<SceneTexture> captor = ArgumentCaptor.forClass(SceneTexture.class);
        verify(sceneTextureRepository, times(0)).save(captor.capture());

        assertEquals(expectedErrorMessage, errors.get("imageUpload"));
    }
}
