package nz.ac.canterbury.seng302.homehelper.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.stream.Stream;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import nz.ac.canterbury.seng302.homehelper.repository.TagRepository;
import nz.ac.canterbury.seng302.homehelper.service.TagService;
import nz.ac.canterbury.seng302.homehelper.utils.profanity.ProfanityChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TagServiceTest {

    @Mock
    private ProfanityChecker profanityChecker;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    private static Stream<Arguments> tagNamesNoLetters() {
        return Stream.of(
                Arguments.of("1"),
                Arguments.of("!"),
                Arguments.of(""),
                Arguments.of(""),
                Arguments.of("@"),
                Arguments.of("$"),
                Arguments.of("^"),
                Arguments.of("*"),
                Arguments.of(")"),
                Arguments.of("2"),
                Arguments.of("64"),
                Arguments.of("78"),
                Arguments.of("!@#$"),
                Arguments.of("!23"),
                Arguments.of("103492141*%(#*%#(%)*#%)*#)&%&)#%))@)@)@)@)@!(!)")
        );
    }

    private static Stream<Arguments> validTagNames() {
        return Stream.of(
                Arguments.of("café123"),
                Arguments.of("ï!?"),
                Arguments.of("é#2025"),
                Arguments.of("é"),
                Arguments.of("123straße"),
                Arguments.of("¡fútbol!"),
                Arguments.of("thisisaverycooltag"),
                Arguments.of("hellotowhoeversreadingthesetests"),
                Arguments.of("should tags have spaces?"),
                Arguments.of("1234567890q")
        );
    }

    @BeforeEach
    public void setup() {
        tagService.setProfanityChecker(profanityChecker);
    }

    @ParameterizedTest
    @MethodSource("tagNamesNoLetters")
    public void invalidTagName_validateTag_addsErrorToHashMap(final String tagName) {
        HashMap<String, String> map = new HashMap<>();
        RenovationRecord record = new RenovationRecord("Name", "Description");

        when(profanityChecker.isProfanePerspective(tagName)).thenReturn(false);
        tagService.validateTag(map, record, tagName);

        assertTrue(map.containsKey("tag"));
        assertEquals("tags must contain letters", map.get("tag"));
    }

    @ParameterizedTest
    @MethodSource("validTagNames")
    public void validTagNameMoreThan5Tags_validateTag_addsErrorToHashMap(final String tagName) {
        HashMap<String, String> map = new HashMap<>();
        RenovationRecord record = new RenovationRecord("Name", "Description");
        record.addTag(new Tag("tag1"));
        record.addTag(new Tag("tag2"));
        record.addTag(new Tag("tag3"));
        record.addTag(new Tag("tag4"));
        record.addTag(new Tag("tag5"));

        when(profanityChecker.isProfanePerspective(tagName)).thenReturn(false);
        tagService.validateTag(map, record, tagName);

        assertTrue(map.containsKey("tag"));
        assertEquals("Renovation records cannot have more than 5 tags", map.get("tag"));
    }

    @ParameterizedTest
    @MethodSource("validTagNames")
    public void validTagName_validateTag_noErrorInHashMap(final String tagName) {
        HashMap<String, String> map = new HashMap<>();
        RenovationRecord record = new RenovationRecord("Name", "Description");

        when(profanityChecker.isProfanePerspective(tagName)).thenReturn(false);
        tagService.validateTag(map, record, tagName);

        assertFalse(map.containsKey("tag"));
    }

    @ParameterizedTest
    @MethodSource("validTagNames")
    public void tagAlreadyInRecord_validateTag_addsErrorToHashMap(final String tagName) {
        HashMap<String, String> map = new HashMap<>();
        RenovationRecord record = new RenovationRecord("Name", "Description");
        record.addTag(new Tag(tagName.toLowerCase()));

        when(profanityChecker.isProfanePerspective(tagName)).thenReturn(false);
        tagService.validateTag(map, record, tagName);

        assertTrue(map.containsKey("tag"));
        assertEquals("Cannot add the same tag to a Renovation Record more than once",
                map.get("tag"));
    }

    @ParameterizedTest
    @MethodSource("tagNamesNoLetters")
    public void invalidTagNameMoreThan5Tags_validateTag_addsInvalidTagNameErrorToHashMap(
            final String tagName) {
        HashMap<String, String> map = new HashMap<>();
        RenovationRecord record = new RenovationRecord("Name", "Description");
        record.addTag(new Tag("tag1"));
        record.addTag(new Tag("tag2"));
        record.addTag(new Tag("tag3"));
        record.addTag(new Tag("tag4"));
        record.addTag(new Tag("tag5"));

        when(profanityChecker.isProfanePerspective(tagName)).thenReturn(false);
        tagService.validateTag(map, record, tagName);

        assertTrue(map.containsKey("tag"));
        assertEquals("tags must contain letters", map.get("tag"));
    }

    @Test
    public void profaneTagName_validateTag_addsProfaneErrorToHashMap() {
        HashMap<String, String> map = new HashMap<>();
        RenovationRecord record = new RenovationRecord("Name", "Description");
        // only need one string because the content isn't properly checked using the api, this test is to make sure the isprofane method is actually triggered
        String tagName = "Shit";

        when(profanityChecker.isProfanePerspective(tagName)).thenReturn(true);
        tagService.validateTag(map, record, tagName);

        assertTrue(map.containsKey("tag"));
        assertEquals("The tag entered is profane and does not follow the system language standards",
                map.get("tag"));
    }

}
