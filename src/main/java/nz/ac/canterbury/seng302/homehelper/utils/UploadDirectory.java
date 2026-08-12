package nz.ac.canterbury.seng302.homehelper.utils;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * enum that points to directories in the uploads folder.
 */
public enum UploadDirectory {
    CUSTOM_TEXTURES(Paths.get("textures", "custom"), "textures"),
    SCENES(Paths.get("scenes"), "design_id"),
    PROFILE_PICTURES(Paths.get("profile-images"), "user_profile_image_id"),
    CUSTOM_MODELS(Paths.get("models", "custom"), "model_id"),
    MODEL_THUMBNAILS(Paths.get("model-images"), "customModelImage-"),
    COMPETITIONS(Paths.get("scenes", "competitions"), "competition_design_id"),
    COMPETITION_THUMBNAILS(Paths.get("designPreviewImages", "competitions"),
            "competition_thumbnail_id"),
    DESIGN_THUMBNAILS(Paths.get("designPreviewImages"), "designPreviewImage-"),
    TEMP_CHUNKS(Paths.get("temp-chunks"), "temp-chunks-"),
    PARALLEL_TEXTURES(Paths.get("textures", "parallel"), "parallel_texture_model_id");


    private static final Logger logger = LoggerFactory.getLogger(UploadDirectory.class);
    private final Path targetFolder;
    private final String fileName;
    private String uploadsFolder = "uploads";
    private Path baseDirectory;

    UploadDirectory(Path targetFolder, String fileName) {
        this.targetFolder = targetFolder;
        this.fileName = fileName;
    }

    public Path getAbsolutePath() {
        if (baseDirectory == null) {
            baseDirectory = Paths.get(System.getProperty("user.dir"), uploadsFolder);
        }
        return baseDirectory.resolve(this.targetFolder);
    }

    public Path getRelativePath() {
        return Paths.get(uploadsFolder, targetFolder.toString());
    }

    /**
     * Returns the path to the target folder to be used when saving to the DB which should always
     * start with uploads
     *
     * @return {@code Path} relative path from "uploads" to the target folder
     */
    public Path getRelativePathForDB() {
        return Paths.get("uploads", targetFolder.toString());
    }

    /**
     * Creates a file name based on the supplied id and the target folder
     *
     * @param id the id of the file that you want to name
     * @return a file name
     */
    public String getFileNameFromTargetFolderAndID(long id) {
        return this.fileName + id;
    }

    /**
     * This is so disgusting but is the easiest way to inject the uploads folder into the Enum as It
     * can't be a spring managed bean due to the non-autowirable String in the constructor.
     */
    @Component
    public static class UploadDirectoryInjector {

        @Value("${static.resource.folder}")
        private String uploadsFolder;

        @PostConstruct
        public void injectUploadsFolderAndInitialiseDirectories() throws IOException {
            for (UploadDirectory uploadDirectory : UploadDirectory.values()) {
                uploadDirectory.uploadsFolder = uploadsFolder;
                logger.info("Injecting uploads folder of {} into {}", uploadsFolder,
                        uploadDirectory.targetFolder);
                Files.createDirectories(uploadDirectory.getAbsolutePath());
            }
        }
    }
}
