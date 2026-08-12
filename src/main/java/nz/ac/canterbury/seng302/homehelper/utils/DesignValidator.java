package nz.ac.canterbury.seng302.homehelper.utils;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

/**
 * Provides back end validation for designs.
 */
public class DesignValidator {

    private static final String descriptionTooLongMessage = "Design description must be 512 characters or less";
    private static final String nameEmptyMessage = "Design name cannot be empty";
    private static final String nameTooLongMessage = "Design name must be 255 characters or less";

    private DesignValidator() {
    }

    /**
     * Validate a design and return a HashMap of errors.
     *
     * @param name        - the name of the design
     * @param description - the description of the design
     * @return HashMap<String, String> errors
     */
    public static Map<String, String> validateDesignDetails(String name, String description) {
        Map<String, String> errors = new HashMap<>();
        validateDescription(errors, description);
        validateName(errors, name);
        return errors;
    }

    /**
     * Test the length of the design description, using codePointCount to count any special
     * characters (e.g. emoji) as 1.
     *
     * @param errors      HashMap<String, String> to add errors to
     * @param description String design's description
     */
    public static void validateDescription(Map<String, String> errors, String description) {
        if (description.codePointCount(0, description.length()) > 512) {
            errors.put("description", descriptionTooLongMessage);
        }
    }

    /**
     * Test for empty design name.
     *
     * @param errors HashMap<String, String> to add errors to
     * @param name   String design's name
     */
    public static void validateName(Map<String, String> errors, String name) {
        if (name == null || name.trim().isEmpty()) {
            errors.put("name", nameEmptyMessage);
        } else if (name.codePointCount(0, name.length()) > 255) {
            errors.put("name", nameTooLongMessage);
        }
    }

    /**
     * Validates the uploaded model file and returns a map of validation errors.
     *
     * @param modelGLB the uploaded file to validate
     * @return HashMap containing validation errors (empty if no errors)
     */
    public static Map<String, String> validateModelFile(MultipartFile modelGLB) {
        Map<String, String> errors = new HashMap<>();

        if (modelGLB == null || modelGLB.isEmpty()) {
            errors.put("file_empty", "Model file cannot be empty");
            return errors;
        }

        if (modelGLB.getContentType() == null || modelGLB.getContentType().isEmpty()) {
            errors.put("content_type_empty", "Model file content type cannot be empty");
        }

        if (modelGLB.getContentType() != null &&
                !modelGLB.getContentType().equalsIgnoreCase("model/gltf-binary")) {
            errors.put("content_type_invalid", "Model file content type must be a gltf-binary");
        }

        return errors;
    }

}
