package nz.ac.canterbury.seng302.homehelper.integration.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import javax.imageio.ImageIO;
import nz.ac.canterbury.seng302.homehelper.utils.FileUtilities;
import nz.ac.canterbury.seng302.homehelper.utils.UploadDirectory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FileUtilitiesIntegrationTests {

    @Autowired
    private FileUtilities fileUtilities;

    @BeforeAll
    static void setUp() throws IOException {
        Files.write(UploadDirectory.SCENES.getAbsolutePath().resolve("design_id1.jpeg"),
                smallValidJpeg());
        Files.write(UploadDirectory.DESIGN_THUMBNAILS.getAbsolutePath()
                .resolve("designPreviewImage-1.jpeg"), smallValidJpeg());
        Files.deleteIfExists(UploadDirectory.COMPETITIONS.getAbsolutePath()
                .resolve("competition_design_id3.jpeg"));
        Files.deleteIfExists(UploadDirectory.COMPETITION_THUMBNAILS.getAbsolutePath()
                .resolve("competition_thumbnail_id3.jpeg"));
    }

    @AfterAll
    static void tearDown() throws IOException {
        Files.deleteIfExists(UploadDirectory.SCENES.getAbsolutePath().resolve("design_id1.jpeg"));
        Files.deleteIfExists(UploadDirectory.DESIGN_THUMBNAILS.getAbsolutePath()
                .resolve("designPreviewImage-1.jpeg"));
        Files.deleteIfExists(UploadDirectory.COMPETITIONS.getAbsolutePath()
                .resolve("competition_design_id1.jpeg"));
        Files.deleteIfExists(UploadDirectory.COMPETITION_THUMBNAILS.getAbsolutePath()
                .resolve("competition_thumbnail_id1.jpeg"));
    }

    private static byte[] smallValidJpeg() throws IOException {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, 0xFFFFFF); // white pixel

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", baos);
        return baos.toByteArray();
    }

    @Test
    void duplicateFileForCompetition_FileExists_FilesIsDuplicated() {
        Assertions.assertDoesNotThrow(
                () -> fileUtilities.duplicateFileForCompetition(UploadDirectory.SCENES,
                        UploadDirectory.COMPETITIONS, "design_id1.jpeg", 1L));
        Assertions.assertDoesNotThrow(
                () -> fileUtilities.duplicateFileForCompetition(UploadDirectory.DESIGN_THUMBNAILS,
                        UploadDirectory.COMPETITION_THUMBNAILS,
                        "designPreviewImage-1.jpeg", 1L));
        Assertions.assertTrue(Files.exists(UploadDirectory.COMPETITIONS.getAbsolutePath()
                .resolve("competition_design_id1.jpeg")));
        Assertions.assertTrue(Files.exists(UploadDirectory.COMPETITION_THUMBNAILS.getAbsolutePath()
                .resolve("competition_thumbnail_id1.jpeg")));
    }

    @Test
    void duplicateFileForCompetition_FileDoesNotExist_ThrowsIOExpection() {
        Assertions.assertThrows(IOException.class,
                () -> fileUtilities.duplicateFileForCompetition(UploadDirectory.SCENES,
                        UploadDirectory.COMPETITIONS, "design_id3.jpeg", 3L));
        Assertions.assertThrows(IOException.class,
                () -> fileUtilities.duplicateFileForCompetition(UploadDirectory.DESIGN_THUMBNAILS,
                        UploadDirectory.COMPETITION_THUMBNAILS,
                        "designPreviewImage-3.jpeg", 3L));
        Assertions.assertFalse(Files.exists(UploadDirectory.COMPETITIONS.getAbsolutePath()
                .resolve("competition_design_id3.jpeg")));
        Assertions.assertFalse(Files.exists(UploadDirectory.COMPETITION_THUMBNAILS.getAbsolutePath()
                .resolve("competition_thumbnail_id3.jpeg")));
    }
}
