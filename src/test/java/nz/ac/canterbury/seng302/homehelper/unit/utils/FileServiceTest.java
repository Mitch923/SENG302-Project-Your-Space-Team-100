package nz.ac.canterbury.seng302.homehelper.unit.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Stream;
import nz.ac.canterbury.seng302.homehelper.utils.FileUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.web.multipart.MultipartFile;

public class FileServiceTest {

    private static Stream<Arguments> fileNamesAndExtensions() {
        return Stream.of(
                Arguments.of("image.png", ".png"),
                Arguments.of("image.jpg", ".jpg"),
                Arguments.of("image.svg", ".svg"),
                Arguments.of("typescript_declaration_file.d.ts", ".d.ts"),
                Arguments.of("no_extension", "")
        );
    }

    @ParameterizedTest
    @MethodSource("fileNamesAndExtensions")
    public void fileName_getFileExtensionFromName_returnsExtension(String fileName,
            String extension) {
        assertEquals(extension, FileUtilities.getFileExtensionFromName(fileName));
    }

    @Test
    public void file_generateMultipartFileFromFile_generatesMultipartFile() throws IOException {
        File mockFile = File.createTempFile("testFile", ".txt");
        mockFile.deleteOnExit();
        String mockFileContent = "This is a test file";
        try (FileWriter writer = new FileWriter(mockFile)) {
            writer.write(mockFileContent);
        }

        MultipartFile resultMultipartFile = FileUtilities.generateMultipartFileFromFile(mockFile);

        assertTrue(resultMultipartFile.getOriginalFilename().startsWith("testFile"));
        assertTrue(resultMultipartFile.getOriginalFilename().endsWith(".txt"));
        assertFalse(resultMultipartFile.isEmpty());
        assertEquals(mockFileContent, new String(resultMultipartFile.getBytes()));
        assertEquals("text/plain", resultMultipartFile.getContentType());
    }

    @Test
    public void invalidSuffix_generateMultipartFileFromFile_defaultsToPng() throws IOException {
        File mockFile = File.createTempFile("testFile", ".this_suffix_is_invalid");
        mockFile.deleteOnExit();
        String mockFileContent = "This is a test file";
        try (FileWriter writer = new FileWriter(mockFile)) {
            writer.write(mockFileContent);
        }

        MultipartFile resultMultipartFile = FileUtilities.generateMultipartFileFromFile(mockFile);

        assertEquals("image/png", resultMultipartFile.getContentType());
    }
}
