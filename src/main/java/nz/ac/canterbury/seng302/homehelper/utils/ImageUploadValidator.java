package nz.ac.canterbury.seng302.homehelper.utils;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

public class ImageUploadValidator {

    private static final Logger logger = LoggerFactory.getLogger(ImageUploadValidator.class);

    private static final List<String> allowedContentTypes = Arrays.asList(
            "image/png",
            "image/jpeg",
            "image/jpg",
            "image/svg+xml"
    );

    private static final List<String> textureAllowedContentTypes = Arrays.asList(
            "image/png",
            "image/jpeg",
            "image/jpg"
    );

    private static final Pattern svgPattern = Pattern.compile("<svg[\\s>].*?</svg>",
            Pattern.DOTALL);

    /**
     * Validates Uploaded image is the right format, has data, and is a valid size
     *
     * @param file the multipart file object to validate
     * @return true if the image is valid or false if invalid
     */
    public static boolean validate(Map<String, String> errors, MultipartFile file) {
        boolean validType =
                !file.isEmpty() && hasValidContentType(file) && (isImageFileUsingImageIO(file)
                        || isValidSvg(file));
        if (!validType) {
            errors.put("imageUpload", "Image must be of type png, jpg or svg");
        }

        boolean validSize = !file.isEmpty() && isLessThan10MB(file);
        if (!validSize && errors.containsKey("imageUpload")) {
            errors.put("imageUpload", errors.get("imageUpload") + ". Image must be less than 10MB");
        } else if (!validSize) {
            errors.put("imageUpload", "Image must be less than 10MB");
        }

        return !errors.containsKey("imageUpload");
    }

    public static boolean validateTexture(Map<String, String> errors, MultipartFile file) {
        boolean validType =
                !file.isEmpty() && hasValidContentTypeTexture(file) && (isImageFileUsingImageIO(
                        file));
        if (!validType) {
            errors.put("imageUpload", "Texture file must be of type png or jpg");
        }

        boolean validSize = !file.isEmpty() && isLessThan10MB(file);
        if (!validSize && errors.containsKey("imageUpload")) {
            errors.put("imageUpload",
                    errors.get("imageUpload") + ". File upload must be less than 10MB");
        } else if (!validSize) {
            errors.put("imageUpload", "File Upload must be less than 10MB");
        }

        return !errors.containsKey("imageUpload");
    }

    /**
     * Created with ChatGPT Validates the content of a file matches that of a svg
     *
     * @param file the file to be checked
     * @return true if valid svg false if not valid svg
     */
    public static boolean isValidSvg(MultipartFile file) {

        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".svg")) {
            return false;
        }

        StringBuilder content;
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        } catch (IOException e) {
            return false;
        }

        // Validate if the content contains a proper SVG structure
        return svgPattern.matcher(content.toString()).find();
    }

    public static boolean hasValidContentType(MultipartFile file) {
        return allowedContentTypes.contains(file.getContentType());
    }

    public static boolean hasValidContentTypeTexture(MultipartFile file) {
        return textureAllowedContentTypes.contains(file.getContentType());
    }

    /**
     * Credit to <a href="https://www.baeldung.com/java-test-whether-file-image">This tutorial</a>
     * This method of checking if the image is valid is the most comprehensive but also the least
     * efficient. This should however prevent some spoofing of image content
     *
     * @param file the multipart file to be validated
     * @return true if the image is valid, false if not
     */
    public static boolean isImageFileUsingImageIO(MultipartFile file) {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            return image != null;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Method filled in by ChatGPT with the prompt "fill in this method" + the method signature
     * Returns true if the image is less than 10MB otherwise returns false
     *
     * @param file MultipartFile profile picture uploaded by the user
     * @return boolean value of the file being a valid size
     */
    private static boolean isLessThan10MB(MultipartFile file) {
        final long MAX_SIZE_IN_BYTES = 10 * 1024 * 1024; // 10MB
        logger.info("{} <= {} bytes", file.getSize(), MAX_SIZE_IN_BYTES);
        return file != null && file.getSize() <= MAX_SIZE_IN_BYTES;
    }

}
