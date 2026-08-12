package nz.ac.canterbury.seng302.homehelper.unit.service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import nz.ac.canterbury.seng302.homehelper.utils.DesignValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class RenovationDesignValidatorTest {

    private static final String descriptionTooLongMessage = "Design description must be 512 characters or less";
    private static final String nameEmptyMessage = "Design name cannot be empty";
    private static final String nameTooLongMessage = "Design name must be 255 characters or less";
    private static final String emoji = "🤪";

    private static Stream<Arguments> validDesignNames() {
        return Stream.of(
                Arguments.of("Design Name 1"),
                Arguments.of("a".repeat(255)),
                Arguments.of(emoji.repeat(255)),
                Arguments.of("103492141*%(#*%#(%)*#%)*#)&%&)#%))@)@)@)@)@!(!)"),
                Arguments.of("āēīōū"),
                Arguments.of("안녕 씨발년들아"),
                Arguments.of("رائحتك مثل الجبن")
        );
    }

    private static Stream<Arguments> invalidDesignNames() {
        return Stream.of(
                Arguments.of("", nameEmptyMessage),
                Arguments.of("            ", nameEmptyMessage),
                Arguments.of("\n\n\n\n", nameEmptyMessage),
                Arguments.of("a".repeat(256), nameTooLongMessage),
                Arguments.of(emoji.repeat(256), nameTooLongMessage)
        );
    }

    private static Stream<Arguments> validDesignDescriptions() {
        return Stream.of(
                Arguments.of("Design Description 5"),
                Arguments.of("d".repeat(512)),
                Arguments.of(emoji.repeat(512)),
                Arguments.of("103492141*%(#*%#(%)*#%)*#)&%&)#%))@)@)@)@)@!(!)"),
                Arguments.of("āēīōū"),
                Arguments.of("안녕 씨발년들아"),
                Arguments.of("رائحتك مثل الجبن")
        );
    }

    private static Stream<Arguments> invalidDesignDescriptions() {
        return Stream.of(
                Arguments.of("d".repeat(513), descriptionTooLongMessage),
                Arguments.of(emoji.repeat(513), descriptionTooLongMessage)
        );
    }

    private static Stream<Arguments> validDesigns() {
        return Stream.of(
                Arguments.of("Design Name 1", "Design Description 5"),
                Arguments.of("a".repeat(255), "Design Description 6"),
                Arguments.of(emoji.repeat(255), ""),
                Arguments.of("Design Name 2", "d".repeat(512)),
                Arguments.of("Design Name 3", emoji.repeat(512)),
                Arguments.of("āēīōū", "رائحتك مثل الجبن")
        );
    }

    private static Stream<Arguments> invalidDesigns() {
        return Stream.of(
                Arguments.of("", "Design Description 5", "name", nameEmptyMessage),
                Arguments.of("a".repeat(256), "Design Description 6", "name", nameTooLongMessage),
                Arguments.of("   \n", "", "name", nameEmptyMessage),
                Arguments.of("Design Name 2", "d".repeat(513), "description", descriptionTooLongMessage)
        );
    }

    @ParameterizedTest
    @MethodSource("validDesignNames")
    public void validName_validateName_noErrors(String name) {
        Map<String, String> errors = new HashMap<>();
        DesignValidator.validateName(errors, name);
        Assertions.assertTrue(errors.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("invalidDesignNames")
    public void invalidName_validateName_hasErrors(String name, String errorMessage) {
        Map<String, String> errors = new HashMap<>();
        DesignValidator.validateName(errors, name);
        Assertions.assertTrue(errors.containsKey("name"));
        Assertions.assertEquals(errors.get("name"), errorMessage);
    }

    @ParameterizedTest
    @MethodSource("validDesignDescriptions")
    public void validDescription_validateDescription_noErrors(String description) {
        Map<String, String> errors = new HashMap<>();
        DesignValidator.validateDescription(errors, description);
        Assertions.assertTrue(errors.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("invalidDesignDescriptions")
    public void invalidDescription_validateDescription_hasErrors(String description,
            String errorMessage) {
        Map<String, String> errors = new HashMap<>();
        DesignValidator.validateDescription(errors, description);
        Assertions.assertTrue(errors.containsKey("description"));
        Assertions.assertEquals(errors.get("description"), errorMessage);
    }

    @ParameterizedTest
    @MethodSource("validDesigns")
    public void validDesign_validateDesign_Details_noErrors(String name, String description) {
        Map<String, String> errors = DesignValidator.validateDesignDetails(name, description);
        Assertions.assertTrue(errors.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("invalidDesigns")
    public void invalidDesign_validateDesign_Details_errors(String name, String description, String errorKey, String errorMessage) {
        Map<String, String> errors = DesignValidator.validateDesignDetails(name, description);
        Assertions.assertTrue(errors.containsKey(errorKey));
        Assertions.assertEquals(errors.get(errorKey), errorMessage);
    }

}
