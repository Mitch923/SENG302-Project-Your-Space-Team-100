package nz.ac.canterbury.seng302.homehelper.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Room;
import nz.ac.canterbury.seng302.homehelper.service.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.utils.LocationValidator;
import nz.ac.canterbury.seng302.homehelper.utils.RenovationRecordValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RenovationFormController {

    private final RenovationService renovationService;
    private final UserService userService;
    private final RenovationRecordValidator renovationRecordValidator;
    Logger logger = LoggerFactory.getLogger(RenovationFormController.class);

    @Autowired
    public RenovationFormController(RenovationService renovationService, UserService userService) {
        this.renovationService = renovationService;
        renovationRecordValidator = new RenovationRecordValidator();
        this.userService = userService;
    }

    /**
     * Gets form to be displayed
     *
     * @return thymeleaf createRenovationFormTemplate
     */
    @GetMapping("/createRenovationForm")
    public String form() {
        logger.info("GET /form");
        return "createRenovationFormTemplate";
    }

    /**
     * Posts a form response with renovation name, description, list of room names, and location
     *
     * @param name        name of the renovation
     * @param description description of the renovation
     * @param roomNames   names of the rooms in that renovation
     * @param street      street part of the location
     * @param suburb      suburb part of the location
     * @param city        city part of the location
     * @param postcode    postcode part of the location
     * @param country     country part of the location
     * @param model       (map-like) representation of name, language and isJava boolean for use in
     *                    thymeleaf, with values being set to relevant parameters provided
     * @return thymeleaf createRenovationFormTemplate
     */
    @PostMapping("/createRenovationForm")
    public String submitForm(@RequestParam(name = "name") String name,
            @RequestParam(name = "description") String description,
            @RequestParam(name = "roomNames", required = false) List<String> roomNames,
            @RequestParam(name = "roomName", required = false) String currentRoomName,
            @RequestParam(name = "streetAddress", required = false, defaultValue = "") String street,
            @RequestParam(name = "suburb", required = false, defaultValue = "") String suburb,
            @RequestParam(name = "city", required = false, defaultValue = "") String city,
            @RequestParam(name = "postcode", required = false, defaultValue = "") String postcode,
            @RequestParam(name = "country", required = false, defaultValue = "") String country,
            Model model) {
        logger.info("POST /form");
        RenovationRecord renovationRecord = new RenovationRecord(userService.getLoggedUser(), name,
                description);
        // Location set up and validate
        Location location = new Location(street, suburb, city, postcode, country);
        boolean locationEmpty = LocationValidator.isLocationEmpty(location);

        List<Room> rooms = renovationRecord.replaceRoomsWithSpringFormatted(roomNames);

        HashMap<String, String> errors = renovationRecordValidator.validateRenovationRecord(
                renovationRecord);
        String recordDuplicateError = renovationService.validateUniqueRenovationRecord(
                renovationRecord.getName());
        if (!Objects.equals(recordDuplicateError, "")) {
            errors.put("duplicate", recordDuplicateError);
        }
        if (!locationEmpty) {
            HashMap<String, String> errorsList = LocationValidator.validateLocation(location);
            errors.putAll(errorsList);
        }
        if (!errors.isEmpty()) {
            logger.warn("Error in new renovation record {}", errors);
            model.addAttribute("name", name);
            model.addAttribute("description", description);
            model.addAttribute("roomNames", roomNames);
            model.addAttribute("roomName", currentRoomName);
            model.addAttribute("errors", errors);
            model.addAttribute("descriptionLength", description.length());
            if (!locationEmpty) {
                model.addAttribute("fullAddress", location.getFullAddress());
            }
            model.addAttribute("location", location);
            return "createRenovationFormTemplate";
        }

        RenovationRecord savedRenovation = renovationService.addRenovationRecord(renovationRecord,
                rooms);
        renovationService.setRenovationLocation(savedRenovation, location);

        return "redirect:/viewRenovation/" + savedRenovation.getId();
    }
}
