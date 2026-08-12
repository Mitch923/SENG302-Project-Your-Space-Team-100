package nz.ac.canterbury.seng302.homehelper.unit.utils;


import java.util.List;
import java.util.stream.Stream;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Room;
import nz.ac.canterbury.seng302.homehelper.service.RenovationService;
import nz.ac.canterbury.seng302.homehelper.utils.RenovationRecordValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.annotation.Import;

@Import(RenovationService.class)
public class ValidationRenovationRecordTest {

    private RenovationRecordValidator renovationRecordValidator;

    private static Stream<Arguments> validationPassRenovationRecords() {
        return Stream.of(
                Arguments.of("RenovationRecord", "Renovating Kitchen"),
                Arguments.of("Renovation record", "Bathroom upgrade with new tiles"),
                Arguments.of("RenovationRecord3", "Adding a new guest bedroom"),
                Arguments.of("Renovation Record 4", "Replacing old flooring in the living room"),
                Arguments.of("-Renovation Record-5-", "Complete repainting of the house"),
                Arguments.of("6Renovation, Record, 6", "Roof maintenance and repair"),
                Arguments.of("Renōvātīōn Rēcōrd 7", "Installing new windows in the office"),
                Arguments.of("Renovat'ion' Record 8.2", "Expanding the backyard patio"),
                Arguments.of("0987654321", "Upgrading electrical wiring for safety"),
                Arguments.of("-' '-", "Modernizing the home with smart devices"),
                Arguments.of("a".repeat(255), "Renovating Kitchen")

        );
    }

    private static Stream<Arguments> validationPassForeignRenovationRecords() {
        return Stream.of(
                Arguments.of("שיא שיפוצים", "Hebrew Renovation"),
                Arguments.of("سجل التجديد", "Arabic Renovation"),
                Arguments.of("改修実績", "Japan Renovation"),
                Arguments.of("Clàr ath-nuadhachadh", "Scottish Gaelic Renovation"),
                Arguments.of("Запис за реновирање", "Macedonian Renovation"),
                Arguments.of("რემონტის ჩანაწერი", "Georgian Renovation"),
                Arguments.of("Hồ sơ cải tạo", "Vietnamese Renovation"),
                Arguments.of("리노베이션 기록", "Korean Renovation"),
                Arguments.of("บันทึกการปรับปรุง", "Thai Renovation"),
                Arguments.of("नवीनीकरण रिकॉर्ड", "Hindi Renovation"),
                Arguments.of("የተሃድሶ መዝገብ", "Amharic Renovation"),
                Arguments.of("Վերանորոգման արձանագրություն", "Armenian Renovation"),
                Arguments.of("সংস্কাৰৰ অভিলেখ", "Assamese Renovation"),
                Arguments.of("翻新記錄", "Cantonese Renovation"),
                Arguments.of("Ҫӗнетӳ ҫырӑвӗ", "Chuvash Renovation"),
                Arguments.of("މަރާމާތު ކުރުމުގެ ރެކޯޑް", "Dhivehi Renovation"),
                Arguments.of("བསྐྱར་བཟོའི་ཟིན་ཐོ", "Dzongkha Renovation"),
                Arguments.of("Ρεκόρ ανακαίνισης", "Greek Renovation"),
                Arguments.of("નવીનીકરણ રેકોર્ડ", "Gujarati Renovation"),
                Arguments.of("ᓄᑖᕈᕆᐊᖅᑕᐅᓂᖏᓐᓄᑦ ᑎᑎᕋᖅᓯᒪᔪᑦ", "Inuktut Renovation"),
                Arguments.of("ನವೀಕರಣ ದಾಖಲೆ", "Kannada Renovation"),
                Arguments.of("កំណត់ត្រាជួសជុលೆ", "Khmer Renovation"),
                Arguments.of("Renovacejis īroksts .", "Latgalian Renovation"),
                Arguments.of("നവീകരണ രേഖ", "Malayalam Renovation"),
                Arguments.of("ꯔꯤꯅꯣꯕꯦꯁꯅꯒꯤ ꯔꯦꯀꯣꯔꯗ", "Meiteilon Renovation"),
                Arguments.of("ပြန်လည်မွမ်းမံမှုမှတ်တမ်း", "Myanmar Renovation"),
                Arguments.of("ߟߊߞߎߘߦߊ ߘߐ߬ߛߙߋ", "NKo Renovation"),
                Arguments.of("Registro de Renovação", "Brazilian Portuguese Renovation"),
                Arguments.of("مرمت دا ریکارڈ", "Punjabi (Shahmukhi) Renovation"),
                Arguments.of("Ođasmahttinreporta", "Sami (North) Renovation"),
                Arguments.of("ᱱᱟᱶᱟᱛᱮᱫ ᱨᱮᱠᱚᱨᱰ", "Santali Renovation"),
                Arguments.of("ප්රතිසංස්කරණ වාර්තාව ", "Sinhala Renovation"),
                Arguments.of("Asekles n useɣnew", "Tamazight Renovation"),
                Arguments.of("ⴰⵙⴻⴽⵍⴻⵙ ⵏ ⵓⵙⴻⴳⴳⴻⵎ", "Tamazight (Tifinagh) Renovation"),
                Arguments.of("புதுப்பித்தல் பதிவு", "Tamil Renovation"),
                Arguments.of("పునరుద్ధరణ రికార్డు", "Telugu Renovation"),
                Arguments.of("ཉམས་གསོའི་ཟིན་ཐོ", "Tibetan Renovation"),
                Arguments.of("መዝገብ ምሕዳስ", "Tigrinya Renovation")
        );
    }

    private static Stream<Arguments> EmptyName_RenovationRecords() {
        return Stream.of(
                Arguments.of("", "Renovating Kitchen"),
                Arguments.of(" ", "Bathroom upgrade with new tiles"),
                Arguments.of("     ", "Adding a new guest bedroom")
        );
    }

    private static Stream<Arguments> TooManyCharsName_RenovationRecords() {
        return Stream.of(
                Arguments.of("a".repeat(256), "Renovating Kitchen"),
                Arguments.of("😅".repeat(256), "Renovating Kitchen")
        );
    }

    private static Stream<Arguments> InvalidCharacterName_RenovationRecords() {
        return Stream.of(
                Arguments.of("_Renovation Record 1", "Renovating Kitchen"),
                Arguments.of("    _    ", "Bathroom upgrade with new tiles"),
                Arguments.of("@", "Adding a new guest bedroom"),
                Arguments.of("Renovation Record #4", "Replacing old flooring in the living room"),
                Arguments.of("Renov@tion Record^$5", "Complete repainting of the house"),
                Arguments.of("@#$%^&*()", "Roof maintenance and repair"),
                Arguments.of("Renovation Record (7)", "Installing new windows in the office"),
                Arguments.of("Renovation_Record_8", "Expanding the backyard patio"),
                Arguments.of("{Renovation Record #9}", "Upgrading electrical wiring for safety"),
                Arguments.of("<Renovation Record 10>", "Modernizing the home with smart devices"),
                Arguments.of("😅".repeat(255), "Renovating Kitchen"),
                Arguments.of("😤".repeat(255), "Renovating Kitchen"),
                Arguments.of("🥶".repeat(255), "Renovating Kitchen")
        );
    }

    private static Stream<Arguments> InvalidDescriptionLengths() {
        return Stream.of(
                Arguments.of("🤌🏽".repeat(514)),
                Arguments.of("🤌🏽".repeat(513)),
                Arguments.of("😂".repeat(513)),
                Arguments.of("a".repeat(550)),
                Arguments.of("🤌🏽".repeat(600)),
                Arguments.of("g".repeat(10000))
        );
    }

    private static Stream<Arguments> ValidDescriptionLengths() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("🤝".repeat(512)),
                Arguments.of("🤝".repeat(511)),
                Arguments.of("😂a".repeat(25)),
                Arguments.of("a".repeat(159)),
                Arguments.of("g😈".repeat(256)),
                Arguments.of("😈".repeat(256))

        );
    }

    private static Stream<Arguments> ValidRoomNames() {
        RenovationRecord renovationRecord = new RenovationRecord();

        return Stream.of(
                Arguments.of(List.of()), // Empty list
                Arguments.of(List.of(new Room("Room 1", renovationRecord))),
                Arguments.of(List.of(new Room("Room-āēīōūĀĒŪ", renovationRecord))),
                Arguments.of(List.of(new Room("Room3.5.6", renovationRecord))),
                Arguments.of(List.of(new Room("Room, 8'7", renovationRecord))),
                Arguments.of(List.of(new Room("", renovationRecord))), // Empty room name
                Arguments.of(List.of(new Room(" ", renovationRecord))), // Space-only name
                Arguments.of(List.of(new Room("a".repeat(255), renovationRecord))),
                Arguments.of(List.of(
                        new Room("-", renovationRecord),
                        new Room("-", renovationRecord),
                        new Room("-", renovationRecord)
                ))
        );
    }

    private static Stream<Arguments> InvalidRoomNames() {
        RenovationRecord renovationRecord = new RenovationRecord();
        return Stream.of(
                Arguments.of(List.of(new Room("Room #1", renovationRecord))),
                Arguments.of(List.of(new Room("@#$%^&*()", renovationRecord))),
                Arguments.of(List.of(new Room("Room3.5.6*", renovationRecord))),
                Arguments.of(List.of(new Room("Room, 8'7__", renovationRecord))),
                Arguments.of(List.of(
                        new Room("#", renovationRecord),
                        new Room("$", renovationRecord),
                        new Room("^", renovationRecord)
                )),
                Arguments.of(List.of(
                        new Room("😅".repeat(254), renovationRecord),
                        new Room("😤".repeat(254), renovationRecord),
                        new Room("🥶".repeat(254), renovationRecord)
                ))
        );
    }

    private static Stream<Arguments> RoomNameTooLong() {
        RenovationRecord renovationRecord = new RenovationRecord();
        return Stream.of(
                Arguments.of(List.of(new Room("a".repeat(256), renovationRecord)))
        );
    }

    private static Stream<Arguments> validRenovationRecords() {
        return Stream.of(
                Arguments.of("RenovationRecord", "Renovating Kitchen"),
                Arguments.of("Renovation record", "Bathroom upgrade with new tiles"),
                Arguments.of("RenovationRecord3", "Adding a new guest bedroom"),
                Arguments.of("Renovation Record 4", "Replacing old flooring in the living room"),
                Arguments.of("-Renovation Record-5-", "Complete repainting of the house"),
                Arguments.of("6Renovation, Record, 6", "Roof maintenance and repair"),
                Arguments.of("Renōvātīōn Rēcōrd 7", "Installing new windows in the office"),
                Arguments.of("Renovat'ion' Record 8.2", "Expanding the backyard patio"),
                Arguments.of("0987654321", "Upgrading electrical wiring for safety"),
                Arguments.of("-' '-", "Modernizing the home with smart devices")
        );
    }

    private static Stream<Arguments> InvalidRenovationRecords_InvalidCharactersInName() {
        return Stream.of(
                Arguments.of("    _    ", "Bathroom upgrade with new tiles"),
                Arguments.of("@", "Adding a new guest bedroom"),
                Arguments.of("Renov@tion Record^$5", "Complete repainting of the house"),
                Arguments.of("@#$%^&*()", "Roof maintenance and repair"),
                Arguments.of("Renovation Record (7)", "Installing new windows in the office"),
                Arguments.of("Renovation_Record_8", "Expanding the backyard patio"),
                Arguments.of("{Renovation Record #9}", "Upgrading electrical wiring for safety"),
                Arguments.of("<Renovation Record 10>", "Modernizing the home with smart devices")
        );
    }

    private static Stream<Arguments> InvalidRenovationRecords_EmptyName() {
        return Stream.of(
                Arguments.of("        ", "Bathroom upgrade with new tiles"),
                Arguments.of("", "Adding a new guest bedroom"),
                Arguments.of("  ", "Complete repainting of the house"),
                Arguments.of("    ", "Roof maintenance and repair")

        );
    }

    @BeforeEach
    public void initiate() {
        this.renovationRecordValidator = new RenovationRecordValidator();
    }

    @ParameterizedTest
    @MethodSource("validationPassRenovationRecords")
    public void validateRenovationRecordName_ValidNames(String name) {
        Assertions.assertEquals("", renovationRecordValidator.validateRenovationRecordName(name));
    }

    @ParameterizedTest
    @MethodSource("validationPassForeignRenovationRecords")
    public void validateRenovationRecordNameForeign_ValidNames(String name) {
        Assertions.assertEquals("", renovationRecordValidator.validateRenovationRecordName(name));
    }

    @ParameterizedTest
    @MethodSource("EmptyName_RenovationRecords")
    public void validateRenovationRecordName_EmptyNames(String name) {
        Assertions.assertEquals("Renovation record name cannot be empty",
                renovationRecordValidator.validateRenovationRecordName(name));
    }

    @ParameterizedTest
    @MethodSource("TooManyCharsName_RenovationRecords")
    public void validateRenovationRecordName_TooManyCharsName(String name) {
        Assertions.assertEquals("Renovation record name must be 255 characters or less",
                renovationRecordValidator.validateRenovationRecordName(name));
    }

    @ParameterizedTest
    @MethodSource("InvalidCharacterName_RenovationRecords")
    public void validateRenovationRecordName_InvalidCharacterNames(String name) {
        Assertions.assertEquals("Renovation record name must only include letters, " +
                        "numbers, spaces, dots, hyphens or apostrophes",
                renovationRecordValidator.validateRenovationRecordName(name));
    }

    @ParameterizedTest
    @MethodSource("ValidDescriptionLengths")
    public void validateRenovationRecordDescription_ValidDescription(String description) {
        Assertions.assertEquals("",
                renovationRecordValidator.validateDescription(description));
    }

    @ParameterizedTest
    @MethodSource("InvalidDescriptionLengths")
    public void validateRenovationRecordDescription_InvalidDescription(String length) {
        Assertions.assertEquals("Renovation record description must be 512 characters or less",
                renovationRecordValidator.validateDescription(length));
    }

    @ParameterizedTest
    @MethodSource("ValidRoomNames")
    public void validateRooms_ValidRoomNames(List<Room> rooms) {
        Assertions.assertEquals("", renovationRecordValidator.validateRooms(rooms));
    }

    @ParameterizedTest
    @MethodSource("InvalidRoomNames")
    public void validateRooms_InvalidRoomNames(List<Room> rooms) {
        Assertions.assertEquals("Renovation record room names must only include letters, " +
                        "numbers, spaces, dots, hyphens or apostrophes",
                renovationRecordValidator.validateRooms(rooms));
    }

    @ParameterizedTest
    @MethodSource("RoomNameTooLong")
    public void validateRooms_RoomNameTooLong(List<Room> rooms) {
        Assertions.assertEquals("Room name must be 255 characters or less",
                renovationRecordValidator.validateRooms(rooms));
    }

    @ParameterizedTest
    @MethodSource("validRenovationRecords")
    public void validateRenovationRecord_Pass(String name, String description) {
        RenovationRecord renovationRecord = new RenovationRecord(name, description);
        Assertions.assertEquals(0,
                renovationRecordValidator.validateRenovationRecord(renovationRecord).size());
    }

    @ParameterizedTest
    @MethodSource("InvalidRenovationRecords_InvalidCharactersInName")
    public void validateRenovationRecord_InvalidName(String name, String description) {
        String errorString = "Renovation record name must only include letters, " +
                "numbers, spaces, dots, hyphens or apostrophes";
        RenovationRecord renovationRecord = new RenovationRecord(name, description);
        Assertions.assertEquals(errorString,
                renovationRecordValidator.validateRenovationRecord(renovationRecord).get("name"));
    }

    @ParameterizedTest
    @MethodSource("InvalidRenovationRecords_EmptyName")
    public void validateRenovationRecord_EmptyName(String name, String description) {
        String errorString = "Renovation record name cannot be empty";
        RenovationRecord renovationRecord = new RenovationRecord(name, description);
        Assertions.assertEquals(errorString,
                renovationRecordValidator.validateRenovationRecord(renovationRecord).get("name"));
    }


}
