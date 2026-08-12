package nz.ac.canterbury.seng302.homehelper.unit.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import nz.ac.canterbury.seng302.homehelper.utils.ImageUploadValidator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class ImageUploadValidatorUnitTest {

    private static final Path validTestImagesDir = Paths.get(
            System.getProperty("user.dir"),
            "src",
            "test",
            "resources",
            "test.ImageUploadValidator.images"
    );

    private static MultipartFile validPng;
    private static MultipartFile validJpg;
    private static MultipartFile validSvg;

    private static MultipartFile invalidPngType;
    private static MultipartFile invalidJpgType;
    private static MultipartFile invalidSvgType;

    private static MultipartFile invalidPngContent;
    private static MultipartFile invalidJpgContent;
    private static MultipartFile invalidSvgContent;

    private static MultipartFile emptyPngContent;
    private static MultipartFile emptyJpgContent;
    private static MultipartFile emptySvgContent;

    private static MultipartFile tooLargePng;
    private static MultipartFile tooLargeJpg;
    private static MultipartFile tooLargeSvg;

    private static HashMap<String, String> errors;

    /**
     * Helper function modified from ChatGPT for using test files
     *
     * @param fileName name of test file in test.ImageUploadValidator.images
     * @return a MockMultiPartFile filled with data from the test file
     */
    public static MultipartFile getTestMultipartFileFromFile(String fileName, String contentType)
            throws IOException {
        Path filePath = validTestImagesDir.resolve(fileName);
        File file = new File(filePath.toString());
        FileInputStream input = new FileInputStream(file);

        return new MockMultipartFile(
                "testFile", // field name
                file.getName(), // original filename
                contentType, // content type
                input // file content
        );
    }

    /**
     * Helper function to create multipart test files with invalid content This method uses
     * hardcoded random bytes instead of reading an actual file
     *
     * @param fileName    name of the file
     * @param contentType contentType of the file to set
     * @param empty       whether the content is set to empty
     */
    public static MultipartFile getBadContentMultipartFile(String fileName, String contentType,
            boolean empty) {
        byte[] invalidBytes = new byte[]{0x12, 0x34, 0x56, 0x78, (byte) 0x9A,
                (byte) 0xBC}; // Arbitrary invalid content

        return new MockMultipartFile(
                "testFile",
                fileName,
                contentType,
                empty ? new byte[]{} : invalidBytes
        );
    }

    /**
     * Method filled in by ChatGPT by providing the docstring and method signature to it Helper
     * function for generating too large MultipartFiles
     *
     * @param fileName    name of the file
     * @param contentType the mime type to set the file to
     */
    private static MultipartFile getTooLargeMultipartFile(String fileName, String contentType) {
        byte[] tooLargeContent = new byte[10 * 1024 * 1024 + 1]; // 10MB + 1 byte
        return new MockMultipartFile(
                "testFile",
                fileName,
                contentType,
                tooLargeContent
        );
    }

    /**
     * Create dummy files to simulate png, jpg, and svg files. Files created using ChatGPT
     */
    @BeforeAll
    static void setUp() {
        try {
            validPng = getTestMultipartFileFromFile("test.png", "image/png");
            validJpg = getTestMultipartFileFromFile("test.jpeg", "image/jpeg");
            validSvg = getTestMultipartFileFromFile("test.svg", "image/svg+xml");

            invalidPngType = getTestMultipartFileFromFile("test.png", "text/plain");
            invalidJpgType = getTestMultipartFileFromFile("test.jpeg", "image/gif");
            invalidSvgType = getTestMultipartFileFromFile("test.svg", "text/html");

            invalidPngContent = getBadContentMultipartFile("test.png", "image/png", false);
            invalidJpgContent = getBadContentMultipartFile("test.jpeg", "image/jpeg", false);
            invalidSvgContent = getBadContentMultipartFile("test.svg", "image/svg+xml", false);

            emptyPngContent = getBadContentMultipartFile("test.png", "image/png", true);
            emptyJpgContent = getBadContentMultipartFile("test.jpeg", "image/jpeg", true);
            emptySvgContent = getBadContentMultipartFile("test.svg", "image/svg+xml", true);

            tooLargePng = getTooLargeMultipartFile("test.png", "image/png");
            tooLargeJpg = getTooLargeMultipartFile("test.jpeg", "image/jpeg");
            tooLargeSvg = getTooLargeMultipartFile("test.svg", "text/html");

        } catch (IOException e) {
            throw new RuntimeException("Failed to load test files", e);
        }
    }

    @BeforeEach
    void setUpTest() {
        errors = new HashMap<>();
    }

    // <=== PNG TESTS ===>

    @Test
    void ValidateImage_ValidPng_ReturnsTrue() {
        boolean result = ImageUploadValidator.validate(errors, validPng);
        assertTrue(result);
    }

    @Test
    void ValidateImage_InvalidPngContent_ReturnsFalse() {
        boolean result = ImageUploadValidator.validate(errors, invalidPngContent);
        assertFalse(result);
    }

    @Test
    void ValidateImage_InvalidPngType_ReturnsFalse() {
        boolean result = ImageUploadValidator.validate(errors, invalidPngType);
        assertFalse(result);
    }

    @Test
    void ValidateImage_EmptyPngContent_ReturnsFalse() {
        boolean result = ImageUploadValidator.validate(errors, emptyPngContent);
        assertFalse(result);
    }

    // <=== JPG TESTS ===>

    @Test
    void ValidateImage_ValidJpg_ReturnsTrue() {
        boolean result = ImageUploadValidator.validate(errors, validJpg);
        assertTrue(result);
    }

    @Test
    void ValidateImage_InvalidJpgContent_ReturnsFalse() {
        boolean result = ImageUploadValidator.validate(errors, invalidJpgContent);
        assertFalse(result);
    }

    @Test
    void ValidateImage_InvalidJpgType_ReturnsFalse() {
        boolean result = ImageUploadValidator.validate(errors, invalidJpgType);
        assertFalse(result);
    }

    @Test
    void ValidateImage_EmptyJpgContent_ReturnsFalse() {
        boolean result = ImageUploadValidator.validate(errors, emptyJpgContent);
        assertFalse(result);
    }

    // <=== SVG TESTS ===>

    @Test
    void ValidateImage_ValidSvg_ReturnsTrue() {
        boolean result = ImageUploadValidator.validate(errors, validSvg);
        assertTrue(result);
    }

    @Test
    void ValidateImage_InvalidSvgContent_ReturnsFalse() {
        boolean result = ImageUploadValidator.validate(errors, invalidSvgContent);
        assertFalse(result);
    }

    @Test
    void ValidateImage_InvalidSvgType_ReturnsFalse() {
        boolean result = ImageUploadValidator.validate(errors, invalidSvgType);
        assertFalse(result);
    }

    @Test
    void ValidateImage_EmptySvgContent_ReturnsFalse() {
        boolean result = ImageUploadValidator.validate(errors, emptySvgContent);
        assertFalse(result);
    }

    @Test
    void validateImage_TooLargePng_ReturnsFalse() {
        boolean result = ImageUploadValidator.validate(errors, tooLargePng);
        assertFalse(result);
    }

    @Test
    void validateImage_TooLargeJpg_ReturnsFalse() {
        boolean result = ImageUploadValidator.validate(errors, tooLargeJpg);
        assertFalse(result);
    }

    @Test
    void validateImage_TooLargeSvg_ReturnsFalse() {
        boolean result = ImageUploadValidator.validate(errors, tooLargeSvg);
        assertFalse(result);
    }

    @Test
    void validateImage_TooLargePng_HasCorrectError() {
        ImageUploadValidator.validate(errors, tooLargePng);
        assertTrue(errors.containsKey("imageUpload"));
        assertTrue(errors.get("imageUpload").contains("Image must be less than 10MB"));
    }

    @Test
    void validateImage_TooLargeJpg_HasCorrectError() {
        ImageUploadValidator.validate(errors, tooLargeJpg);
        assertTrue(errors.containsKey("imageUpload"));
        assertTrue(errors.get("imageUpload").contains("Image must be less than 10MB"));
    }

    @Test
    void validateImage_TooLargeSvg_HasCorrectError() {
        ImageUploadValidator.validate(errors, tooLargeSvg);
        assertTrue(errors.containsKey("imageUpload"));
        assertTrue(errors.get("imageUpload").contains("Image must be less than 10MB"));
    }

    // <=== Validate Texture Tests ===>

    @Test
    void ValidateTexture_ValidPng_ReturnsTrue() {
        boolean result = ImageUploadValidator.validateTexture(errors, validPng);
        assertTrue(result);
    }

    @Test
    void ValidateTexture_InvalidPngContent_ReturnsFalse() {
        boolean result = ImageUploadValidator.validateTexture(errors, invalidPngContent);
        assertFalse(result);
    }

    @Test
    void ValidateTexture_InvalidPngType_ReturnsFalse() {
        boolean result = ImageUploadValidator.validateTexture(errors, invalidPngType);
        assertFalse(result);
    }

    @Test
    void ValidateTexture_EmptyPngContent_ReturnsFalse() {
        boolean result = ImageUploadValidator.validateTexture(errors, emptyPngContent);
        assertFalse(result);
    }

    // <=== JPG TESTS ===>

    @Test
    void ValidateTexture_ValidJpg_ReturnsTrue() {
        boolean result = ImageUploadValidator.validateTexture(errors, validJpg);
        assertTrue(result);
    }

    @Test
    void ValidateTexture_InvalidJpgContent_ReturnsFalse() {
        boolean result = ImageUploadValidator.validateTexture(errors, invalidJpgContent);
        assertFalse(result);
    }

    @Test
    void ValidateTexture_InvalidJpgType_ReturnsFalse() {
        boolean result = ImageUploadValidator.validateTexture(errors, invalidJpgType);
        assertFalse(result);
    }

    @Test
    void ValidateTexture_EmptyJpgContent_ReturnsFalse() {
        boolean result = ImageUploadValidator.validateTexture(errors, emptyJpgContent);
        assertFalse(result);
    }

    // <=== SVG TESTS ===>

    @Test
    void ValidateTexture_ValidSvg_ReturnsFalse() {
        boolean result = ImageUploadValidator.validateTexture(errors, validSvg);
        assertFalse(result);
    }

    @Test
    void ValidateTexture_InvalidSvgContent_ReturnsFalse() {
        boolean result = ImageUploadValidator.validateTexture(errors, invalidSvgContent);
        assertFalse(result);
    }

    @Test
    void ValidateTexture_InvalidSvgType_ReturnsFalse() {
        boolean result = ImageUploadValidator.validateTexture(errors, invalidSvgType);
        assertFalse(result);
    }

    @Test
    void ValidateTexture_EmptySvgContent_ReturnsFalse() {
        boolean result = ImageUploadValidator.validateTexture(errors, emptySvgContent);
        assertFalse(result);
    }

    @Test
    void ValidateTexture_TooLargePng_ReturnsFalse() {
        boolean result = ImageUploadValidator.validateTexture(errors, tooLargePng);
        assertFalse(result);
    }

    @Test
    void ValidateTexture_TooLargeJpg_ReturnsFalse() {
        boolean result = ImageUploadValidator.validateTexture(errors, tooLargeJpg);
        assertFalse(result);
    }

    @Test
    void ValidateTexture_TooLargeSvg_ReturnsFalse() {
        boolean result = ImageUploadValidator.validateTexture(errors, tooLargeSvg);
        assertFalse(result);
    }

    @Test
    void ValidateTexture_TooLargePng_HasCorrectError() {
        ImageUploadValidator.validateTexture(errors, tooLargePng);
        assertTrue(errors.containsKey("imageUpload"));
        assertTrue(errors.get("imageUpload").contains("File upload must be less than 10MB"));
    }

    @Test
    void ValidateTexture_TooLargeJpg_HasCorrectError() {
        ImageUploadValidator.validateTexture(errors, tooLargeJpg);
        assertTrue(errors.containsKey("imageUpload"));
        assertTrue(errors.get("imageUpload").contains("File upload must be less than 10MB"));
    }

    @Test
    void ValidateTexture_TooLargeSvg_HasCorrectError() {
        ImageUploadValidator.validateTexture(errors, tooLargeSvg);
        assertTrue(errors.containsKey("imageUpload"));
        assertTrue(errors.get("imageUpload").contains(
                "Texture file must be of type png or jpg. File upload must be less than 10MB"));
    }

}
