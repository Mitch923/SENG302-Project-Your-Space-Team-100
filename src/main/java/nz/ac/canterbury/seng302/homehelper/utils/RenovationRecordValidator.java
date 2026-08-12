package nz.ac.canterbury.seng302.homehelper.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Room;

public class RenovationRecordValidator {

    // Allows letters, digits, marks and the punctuation . , spaces, -. ་ is an international space character
    private final String regexName = "[\\p{L}\\p{N}\\p{M}.,' ་-]+";
    private final Pattern pattern;

    public RenovationRecordValidator() {
        this.pattern = Pattern.compile(regexName);
    }

    /**
     * Validates the renovation record and any associated rooms
     *
     * @param renovationRecord RenovationRecord object to be validated
     * @return List<String> of error messages, or empty list if no errors were found
     */
    public HashMap<String, String> validateRenovationRecord(RenovationRecord renovationRecord) {
        HashMap<String, String> errors = new HashMap<>();
        String recordNameError = validateRenovationRecordName(renovationRecord.getName());
        String recordDescriptionError = validateDescription(
                renovationRecord.getDescription());
        String roomsError = validateRooms(renovationRecord.getRooms());

        if (!Objects.equals(recordNameError, "")) {
            errors.put("name", recordNameError);
        }
        if (!Objects.equals(recordDescriptionError, "")) {
            errors.put("description", recordDescriptionError);
        }
        if (!Objects.equals(roomsError, "")) {
            errors.put("rooms", roomsError);
        }

        return errors;
    }

    /**
     * Validates that a name is not empty or using invalid characters
     *
     * @param name The RenovationRecord name as a String
     * @return Empty string if no error, or an error message if there is an error
     */
    public String validateRenovationRecordName(String name) {
        String error = "";
        Matcher matcher = pattern.matcher(name);
        boolean nameOkay = matcher.matches();
        if (name.trim().isEmpty()) {
            error = "Renovation record name cannot be empty";
        } else if (name.codePointCount(0, name.length()) > 255) {
            error = "Renovation record name must be 255 characters or less";
        } else if (!nameOkay) {
            error = "Renovation record name must only include letters, " +
                    "numbers, spaces, dots, hyphens or apostrophes";
        }
        return error;
    }

    /**
     * Validates the length of the description in the RenovationRecord
     *
     * @param description int length of the description in the RenovationRecord
     * @return Empty string if no error, or an error message if there is an error
     */
    public String validateDescription(String description) {
        String error = "";
        String cleanedDescription = description.replaceAll("\\r", "");
        if (cleanedDescription.codePointCount(0, cleanedDescription.length()) > 512) {
            error = "Renovation record description must be 512 characters or less";
        }
        return error;
    }

    /**
     * Validates that the names of the rooms are not using invalid characters
     *
     * @param rooms List<Room> containing all the Room's to be validated
     * @return Empty string if no error, or an error message if there is an error with any room in
     * the list
     */
    public String validateRooms(List<Room> rooms) {
        String error = "";
        if (rooms != null && !rooms.isEmpty()) {
            for (Room room : rooms) {
                Matcher matcher = pattern.matcher(room.getName());
                boolean nameOkay = matcher.matches();
                if (room.getName().codePointCount(0, room.getName().length()) > 255) {
                    error = "Room name must be 255 characters or less";
                } else if (!nameOkay && !room.getName().isEmpty()) {
                    error = "Renovation record room names must only include letters, " +
                            "numbers, spaces, dots, hyphens or apostrophes";
                }
            }
        }
        return error;
    }

}


