package nz.ac.canterbury.seng302.homehelper.unit.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.stream.Stream;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.utils.LocationValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LocationValidatorUnitTest {

    private static final String inputTooLong = "a".repeat(256);

    private static Stream<Arguments> validLocations() {
        return Stream.of(
                Arguments.of("21 Main St", "Suburbville", "CityTown", "1234", "NZ"),
                Arguments.of("1234", "Suburbville", "CityTown", "1234", "NZ"),
                Arguments.of("Main-St", "Suburbville", "CityTown", "1234", "NZ"),
                Arguments.of("/-. '", "Suburbville", "CityTown", "1234", "NZ"),
                Arguments.of("21 Main St", "", "CityTown", "1234", "NZ"),
                Arguments.of("21 Main St", "/-. '", "CityTown", "1234", "NZ"),
                Arguments.of("21 Main St", "Suburbville", "/-. '", "1234", "NZ"),
                Arguments.of("21 Main St", "Suburbville", "CityTown", "1234", "/-. '"),
                Arguments.of("21 Main St", "Suburbville", "CityTown", "123 456", "NZ")
        );
    }

    private static Stream<Arguments> emptyLocations() {
        return Stream.of(
                Arguments.of("", "", "", "", ""),
                Arguments.of(" ", " ", " ", " ", " "),
                Arguments.of("      ", "      ", "     ", "      ", "      "),
                Arguments.of("      ", "", "     ", "      ", "      ")
        );
    }

    private static Stream<Arguments> emptyLocationField() {
        return Stream.of(
                Arguments.of("", "Suburbville", "", "", ""),
                Arguments.of("", "Suburbville", "CityTown", "1234", "NZ"),
                Arguments.of(" ", "Suburbville", "CityTown", "1234", "NZ"),
                Arguments.of("      ", "Suburbville", "CityTown", "1234", "NZ"),
                Arguments.of("21 Main St", "Suburbville", "", "1234", "NZ"),
                Arguments.of("21 Main St", "Suburbville", " ", "1234", "NZ"),
                Arguments.of("21 Main St", "Suburbville", "         ", "1234", "NZ"),
                Arguments.of("21 Main St", "Suburbville", "CityTown", "", "NZ"),
                Arguments.of("21 Main St", "Suburbville", "CityTown", " ", "NZ"),
                Arguments.of("21 Main St", "Suburbville", "CityTown", "      ", "NZ"),
                Arguments.of("21 Main St", "Suburbville", "CityTown", "1234", ""),
                Arguments.of("21 Main St", "Suburbville", "CityTown", "1234", " "),
                Arguments.of("21 Main St", "Suburbville", "CityTown", "1234", "     ")
        );
    }

    private static Stream<Arguments> validCountriesSuburbsCities() {
        return Stream.of(
                Arguments.of("Papua New Guinea"),
                Arguments.of("France"),
                Arguments.of("New Zealand"),
                Arguments.of("This is a long country"),
                Arguments.of("México"),
                Arguments.of("Nigería")
        );
    }

    private static Stream<Arguments> invalidCountries() {
        return Stream.of(
                Arguments.of("Papua  New Guinea"),
                Arguments.of("Fran  ce"),
                Arguments.of("New  Zealand"),
                Arguments.of("This is a long country??"),
                Arguments.of("México@"),
                Arguments.of("Nig3ría")
        );
    }

    private static Stream<Arguments> invalidStreets() {
        return Stream.of(
                Arguments.of("23 M@in St"),
                Arguments.of("$%*()")
        );
    }

    private static Stream<Arguments> invalidSuburbs() {
        return Stream.of(
                Arguments.of("23 Suburbville"),
                Arguments.of("$%*()")
        );
    }

    @ParameterizedTest
    @MethodSource("validLocations")
    void validateLocation_allInputsValid_errorsPresent(String street, String suburb, String city,
            String postcode, String country) {
        Location location = new Location(street, suburb, city, postcode, country);
        HashMap<String, String> locationErrors = LocationValidator.validateLocation(location);
        assertTrue(locationErrors.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("emptyLocations")
    void validateLocation_allInputsEmpty_noErrors(String street, String suburb, String city,
            String postcode, String country) {
        Location location = new Location(street, suburb, city, postcode, country);
        HashMap<String, String> locationErrors = LocationValidator.validateLocation(location);
        assertFalse(locationErrors.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("emptyLocationField")
    void validateLocation_oneInputEmpty_noErrors(String street, String suburb, String city,
            String postcode, String country) {
        Location location = new Location(street, suburb, city, postcode, country);
        HashMap<String, String> locationErrors = LocationValidator.validateLocation(location);
        assertFalse(locationErrors.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("emptyLocations")
    void isLocationEmpty_locationEmpty_returnTrue(String street, String suburb, String city,
            String postcode, String country) {
        Location location = new Location(street, suburb, city, postcode, country);
        boolean isLocationEmpty = LocationValidator.isLocationEmpty(location);
        assertTrue(isLocationEmpty);
    }

    @ParameterizedTest
    @MethodSource("validLocations")
    void isLocationEmpty_locationNotEmpty_returnFalse(String street, String suburb, String city,
            String postcode, String country) {
        Location location = new Location(street, suburb, city, postcode, country);
        boolean isLocationEmpty = LocationValidator.isLocationEmpty(location);
        assertFalse(isLocationEmpty);
    }

    @ParameterizedTest
    @MethodSource("validCountriesSuburbsCities")
    void validCountry_validateLocation_noErrors(String country) {
        Location location = new Location("Street", "Suburbville", "CityTown", "1234", country);
        HashMap<String, String> errors = LocationValidator.validateLocation(location);
        assertTrue(errors.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("validCountriesSuburbsCities")
    void validSuburb_validateLocation_noErrors(String suburb) {
        Location location = new Location("Street", suburb, "CityTown", "1234", "NZ");
        HashMap<String, String> errors = LocationValidator.validateLocation(location);
        assertTrue(errors.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("validCountriesSuburbsCities")
    void validCity_validateLocation_noErrors(String city) {
        Location location = new Location("Street", "Suburb", city, "1234", "NZ");
        HashMap<String, String> errors = LocationValidator.validateLocation(location);
        assertTrue(errors.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("invalidCountries")
    void invalidCountry_validateLocation_hasError(String country) {
        Location location = new Location("Street", "Suburbville", "CityTown", "1234", country);
        HashMap<String, String> errors = LocationValidator.validateLocation(location);
        assertTrue(errors.containsKey("countryError"));
        assertEquals("Country contains invalid characters.", errors.get("countryError"));
    }

    @ParameterizedTest
    @MethodSource("invalidStreets")
    void invalidStreet_validateLocation_hasError(String street) {
        Location location = new Location(street, "Suburbville", "CityTown", "1234", "NZ");
        HashMap<String, String> errors = LocationValidator.validateLocation(location);
        assertTrue(errors.containsKey("streetError"));
        assertEquals("Street address contains invalid characters.", errors.get("streetError"));
    }

    @ParameterizedTest
    @MethodSource("invalidSuburbs")
    void invalidSuburb_validateLocation_hasError(String suburb) {
        Location location = new Location("21 Main St", suburb, "CityTown", "1234", "NZ");
        HashMap<String, String> errors = LocationValidator.validateLocation(location);
        assertTrue(errors.containsKey("suburbError"));
        assertEquals("Suburb contains invalid characters.", errors.get("suburbError"));
    }

    @Test
    void streetTooLong_validateLocation_hasError() {
        Location location = new Location(inputTooLong, "Suburbville", "CityTown", "1234", "NZ");
        HashMap<String, String> errors = LocationValidator.validateLocation(location);
        assertTrue(errors.containsKey("streetError"));
        assertEquals("Street length must be less than 256 characters.", errors.get("streetError"));
    }

    @Test
    void suburbTooLong_validateLocation_hasError() {
        Location location = new Location("21 Main St", inputTooLong, "CityTown", "1234", "NZ");
        HashMap<String, String> errors = LocationValidator.validateLocation(location);
        assertTrue(errors.containsKey("suburbError"));
        assertEquals("Suburb length must be less than 256 characters.", errors.get("suburbError"));
    }

    @Test
    void cityTooLong_validateLocation_hasError() {
        Location location = new Location("21 Main St", "Suburbville", inputTooLong, "1234", "NZ");
        HashMap<String, String> errors = LocationValidator.validateLocation(location);
        assertTrue(errors.containsKey("cityError"));
        assertEquals("City length must be less than 256 characters.", errors.get("cityError"));
    }

    @Test
    void postcodeTooLong_validateLocation_hasError() {
        Location location = new Location("21 Main St", "Suburbville", "CityTown", inputTooLong,
                "NZ");
        HashMap<String, String> errors = LocationValidator.validateLocation(location);
        assertTrue(errors.containsKey("postcodeError"));
        assertEquals("Postcode length must be less than 256 characters.",
                errors.get("postcodeError"));
    }

    @Test
    void countryTooLong_validateLocation_hasError() {
        Location location = new Location("21 Main St", "Suburbville", "CityTown", "1234",
                inputTooLong);
        HashMap<String, String> errors = LocationValidator.validateLocation(location);
        assertTrue(errors.containsKey("countryError"));
        assertEquals("Country length must be less than 256 characters.",
                errors.get("countryError"));
    }

    @Test
    void suburbEmpty_validateLocation_noError() {
        Location location = new Location("21 Main St", "", "CityTown", "1234", "NZ");
        HashMap<String, String> errors = LocationValidator.validateLocation(location);
        assertTrue(errors.isEmpty());
    }

    @Test
    void emptyStreet_validateLocation_hasError() {
        Location location = new Location("", "Suburbville", "CityTown", "1234", "NZ");
        HashMap<String, String> errors = LocationValidator.validateLocation(location);
        assertTrue(errors.containsKey("streetError"));
        assertEquals("Street cannot be empty.", errors.get("streetError"));
    }

    @Test
    void emptyCity_validateLocation_hasError() {
        Location location = new Location("21 Main St", "Suburbville", "", "1234", "NZ");
        HashMap<String, String> errors = LocationValidator.validateLocation(location);
        assertTrue(errors.containsKey("cityError"));
        assertEquals("City cannot be empty.", errors.get("cityError"));
    }

    @Test
    void emptyPostcode_validateLocation_hasError() {
        Location location = new Location("21 Main St", "Suburbville", "CityTown", "", "NZ");
        HashMap<String, String> errors = LocationValidator.validateLocation(location);
        assertTrue(errors.containsKey("postcodeError"));
        assertEquals("Postcode cannot be empty.", errors.get("postcodeError"));
    }

    @Test
    void emptyCountry_validateLocation_hasError() {
        Location location = new Location("21 Main St", "Suburbville", "CityTown", "1234", "");
        HashMap<String, String> errors = LocationValidator.validateLocation(location);
        assertTrue(errors.containsKey("countryError"));
        assertEquals("Country cannot be empty.", errors.get("countryError"));
    }

}
