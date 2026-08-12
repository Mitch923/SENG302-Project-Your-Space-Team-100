package nz.ac.canterbury.seng302.homehelper.utils;

import java.util.HashMap;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validator to validate Location entities.
 */
public class LocationValidator {

    private static final Logger logger = LoggerFactory.getLogger(LocationValidator.class);

    private static final String STREET_REGEX = "^[\\p{L}\\p{M}\\d.'\\-/ ]+$";
    private static final String SUBURB_REGEX = "^[\\p{L}\\p{M}.'\\-/ ]+$";
    private static final String CITY_REGEX = "^[\\p{L}\\p{M}.'\\-/ ]+$";
    private static final String POSTCODE_REGEX = "^[\\p{L}\\p{M}\\d.'\\-/ ]+$";
    private static final String COUNTRY_REGEX = "^[\\p{L}\\p{M}.'\\-/ ]+$";

    /**
     * Private constructor to prevent initialisation
     */
    private LocationValidator() {
    }

    /**
     * Validates all the fields of a Location (street, suburb, city, postcode, country)
     *
     * @param location Location
     * @return HashMap<String, String> containing any relevant error messages
     */
    public static HashMap<String, String> validateLocation(Location location) {
        HashMap<String, String> errors = new HashMap<>();
        boolean postcodeHasConsecutiveSpace = location.getPostCode().contains("  ");
        boolean countryHasConsecutiveSpace = location.getCountry().contains("  ");
        if (location.getStreet().length() > 255) {
            errors.put("streetError", "Street length must be less than 256 characters.");
        } else if (location.getStreet().isEmpty()) {
            errors.put("streetError", "Street cannot be empty.");
        } else if (!(location.getStreet().matches(STREET_REGEX))) {
            errors.put("streetError", "Street address contains invalid characters.");
        }

        if (location.getSuburb().length() > 255) {
            errors.put("suburbError", "Suburb length must be less than 256 characters.");
        } else if (!(location.getSuburb().matches(SUBURB_REGEX)) && !location.getSuburb()
                .isEmpty()) {
            errors.put("suburbError", "Suburb contains invalid characters.");
        }

        if (location.getCity().length() > 255) {
            errors.put("cityError", "City length must be less than 256 characters.");
        } else if (location.getCity().isEmpty()) {
            errors.put("cityError", "City cannot be empty.");
        } else if (!location.getCity().matches(CITY_REGEX)) {
            errors.put("cityError", "City contains invalid characters.");
        }

        if (location.getPostCode().length() > 255) {
            errors.put("postcodeError", "Postcode length must be less than 256 characters.");
        } else if (location.getPostCode().isEmpty()) {
            errors.put("postcodeError", "Postcode cannot be empty.");
        } else if (!location.getPostCode().matches(POSTCODE_REGEX) || postcodeHasConsecutiveSpace) {
            errors.put("postcodeError", "Postcode contains invalid characters.");
        }

        if (location.getCountry().length() > 255) {
            errors.put("countryError", "Country length must be less than 256 characters.");
        } else if (location.getCountry().isEmpty()) {
            errors.put("countryError", "Country cannot be empty.");
        } else if (!location.getCountry().matches(COUNTRY_REGEX) || countryHasConsecutiveSpace) {
            errors.put("countryError", "Country contains invalid characters.");
        }
        logger.info(errors.toString());
        return errors;
    }

    /**
     * Test if a Location contains only empty strings or whitespace.
     *
     * @param location Location object
     * @return boolean, true if location is empty
     */
    public static boolean isLocationEmpty(Location location) {
        boolean locationEmpty = true;
        if (!location.getStreet().trim().isEmpty()) {
            locationEmpty = false;
        } else if (!location.getSuburb().trim().isEmpty()) {
            locationEmpty = false;
        } else if (!location.getCity().trim().isEmpty()) {
            locationEmpty = false;
        } else if (!location.getPostCode().trim().isEmpty()) {
            locationEmpty = false;
        } else if (!location.getCountry().trim().isEmpty()) {
            locationEmpty = false;
        }
        return locationEmpty;
    }
}
