package nz.ac.canterbury.seng302.homehelper.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import nz.ac.canterbury.seng302.homehelper.dto.UserDTO;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.SpringEmailService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.utils.ImageUploadValidator;
import nz.ac.canterbury.seng302.homehelper.utils.LocationValidator;
import nz.ac.canterbury.seng302.homehelper.utils.UserDataValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * This class handles both displaying the user current information including the profile picture and
 * via a separate set of endpoints editing these same fields and updating the users password.
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final PasswordEncoder passwordEncoder;
    private final SpringEmailService springEmailService;
    UserService userService;
    Logger logger = LoggerFactory.getLogger(ProfileController.class);

    @Value("${static.resource.folder}")
    private String uploadsFolder;

    @Autowired
    public ProfileController(UserService userService, PasswordEncoder passwordEncoder,
            SpringEmailService springEmailService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.springEmailService = springEmailService;
    }

    /**
     * Loads the profile page which shows the first name, last name and email of the user
     *
     * @param model the model containing the User object whose attributes will be mapped to the
     *              correct fields
     * @return the html template of the profile page
     */
    @GetMapping
    public String profile(Model model) {
        User user = userService.getLoggedUser();
        if (user.getUserLocation() != null) {
            String address =
                    user.getUserLocation().getCity() + ", " + user.getUserLocation().getCountry();
            model.addAttribute("locationFullAddress", address);
        }
        model.addAttribute("user", user);
        return "profile";
    }

    /**
     * Loads the edit profile page with the details of the user prepopulated in the input fields
     *
     * @param model the model containing the User object whose attributes will be mapped to the
     *              correct fields
     * @return editProfile name of the html template to be loaded
     */
    @GetMapping("/edit")
    public String editProfile(Model model) {
        User user = userService.getLoggedUser();
        Location location = user.getUserLocation();
        if (location != null) {
            model.addAttribute("location", location);
            model.addAttribute("fullAddress", location.getFullAddress());
        }
        model.addAttribute("user", user);
        model.addAttribute("imageToUse", "userImage");
        return "editProfile";
    }

    /**
     * Handles POST requests to edit a user in the database
     *
     * @param firstName form first name
     * @param lastName  form last name
     * @param email     form email
     * @param street    street part of the location
     * @param suburb    suburb part of the location
     * @param city      city part of the location
     * @param postcode  postcode part of the location
     * @param country   country part of the location
     * @return thymeleaf template
     */
    @PostMapping("/edit")
    public String updateProfile(@RequestParam(name = "firstName") String firstName,
            @RequestParam(name = "lastName") String lastName,
            @RequestParam(name = "email") String email,
            @RequestParam(name = "file") MultipartFile file,
            @RequestParam(name = "useFailSubmissionImage") String useFailSubmissionImage,
            @RequestParam(name = "streetAddress", required = false, defaultValue = "") String street,
            @RequestParam(name = "suburb", required = false, defaultValue = "") String suburb,
            @RequestParam(name = "city", required = false, defaultValue = "") String city,
            @RequestParam(name = "postcode", required = false, defaultValue = "") String postcode,
            @RequestParam(name = "country", required = false, defaultValue = "") String country,
            Model model) throws IOException {
        logger.info("POST /profile/edit");

        model.addAttribute("imageToUse", "userImage");
        UserDTO userDTO = new UserDTO(firstName, lastName, email, "", "", "", "", "");
        Map<String, String> errors = UserDataValidator.validateEditUser(userDTO, userService);

        // Location set up and validate
        Location location = new Location(street, suburb, city, postcode, country);
        boolean locationEmpty = LocationValidator.isLocationEmpty(location);
        if (!locationEmpty) {
            errors.putAll(LocationValidator.validateLocation(location));
            String suburbFormat = !suburb.isEmpty() ? suburb + ", " : "";
            String fullAddress =
                    street + ", " + suburbFormat + city + ", " + postcode + ", " + country;
            model.addAttribute("fullAddress", fullAddress);
        }

        if (!errors.isEmpty()) { // Validation of the other inputs
            logger.info("Invalid user data: {}, {}, {}. Location: {}", firstName, lastName, email,
                    location.getFullAddress());
            model.addAttribute("errors", errors);
            User user = userService.getLoggedUser();
            model.addAttribute("user", user);
            addInputsToModel(model, firstName, lastName, email, file, useFailSubmissionImage,
                    street, suburb, city, postcode, country);
        }

        if (!file.isEmpty() && !ImageUploadValidator.validate(errors, file) && Objects.equals(
                useFailSubmissionImage,
                "false")) { // Validate the image when it is included in the form
            logger.info("Invalid image file submitted");
            model.addAttribute("errors", errors);
            User user = userService.getLoggedUser();
            model.addAttribute("user", user);
            addInputsToModel(model, firstName, lastName, email, file, useFailSubmissionImage,
                    street, suburb, city, postcode, country);
        }

        User user = userService.getLoggedUser();
        if (useFailSubmissionImage.equals("true")) {
            logger.info("User has a previous submitted image to use");
            Path path = Paths.get(System.getProperty("user.dir"), uploadsFolder, "/profile-images");
            path = path.resolve(
                    user.getFailSubmissionImagePath().replace("/uploads/profile-images/", ""));
            MultipartFile failImageFile = userService.getMultipartProfileImage(path.toString());

            if (!ImageUploadValidator.validate(errors,
                    failImageFile)) { // Validation of the image when it is not included in the form
                logger.info("Fail Submission Image is invalid");
            }
        }

        if (!errors.isEmpty()) {
            logger.info("Invalid Request: {}", errors);
            model.addAttribute("errors", errors);
            model.addAttribute("user", user);
            addInputsToModel(model, firstName, lastName, email, file, useFailSubmissionImage,
                    street, suburb, city, postcode, country);
            if (errors.containsKey(
                    "imageUpload")) { // If the image in invalid the view should use the user's image
                model.addAttribute("imageToUse", "userImage");
            }
            return "editProfile";
        }

        // At this point everything is valid
        if (useFailSubmissionImage.equals("true")) {
            logger.info("Confirming the users previously submitted valid image");
            userService.confirmFailSubmissionImage();
        } else if (!(file.isEmpty() || file.getSize() == 0)) {
            logger.info("Confirming the users submitted valid image");
            try {
                userService.setUserProfileImage(file);
            } catch (IOException e) {
                logger.warn("Error while setting profile image", e);
            }
            // Overwrites the profileImagePath attribute generated by ControllerAdvisor
            String filePath = userService.getLoggedUser().getProfileImagePath();
            model.addAttribute("profileImagePath",
                    Objects.requireNonNullElse(filePath,
                            "/img/user_profile_images/Default_pfp.svg"));
        }

        String targetEmail = this.userService.getLoggedUser().getEmail();
        logger.info("Updating user with email {} to have values {}, {}, {}", targetEmail, firstName,
                lastName, email);
        this.userService.updateUser(firstName, lastName, email, targetEmail, location);

        model.addAttribute("user", user);
        return "redirect:/profile";
    }

    /**
     * Loads the edit password page where the user can change their password by providing the
     * current password and the new password
     *
     * @param model Spring Model to handle passing the user data to the view
     * @return string containing the name of the template to be loaded
     */
    @GetMapping("/editPassword")
    public String editPasswordPage(Model model) {
        logger.info("GET /profile/editPassword");
        User user = userService.getLoggedUser();

        model.addAttribute("user", user);
        return "editPasswordPage";
    }

    /**
     * Updates the users password assuming the new password passes validation
     *
     * @param oldPassword       the users current password
     * @param newPassword       the password the user wants to change too
     * @param repeatNewPassword the previous password repeated to ensure no miss type
     * @param model             model attached to the template to pass errors back into
     * @return name of the template to return or a string containing redirect to the profile page
     */
    @PostMapping("/editPassword")
    public String editPassword(@RequestParam(name = "oldPassword") String oldPassword,
            @RequestParam(name = "password") String newPassword,
            @RequestParam(name = "confirm") String repeatNewPassword,
            Model model) {
        User user = userService.getLoggedUser();
        Map<String, String> errors = new HashMap<>();
        UserDataValidator.validateEditPassword(errors, passwordEncoder, oldPassword, newPassword,
                repeatNewPassword, user);
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("user", user);
            return "editPasswordPage";
        }
        springEmailService.sendPasswordChangeEmail(user.getFirstName(), user.getEmail());
        userService.updateUserPassword(user.getId(), newPassword);
        return "redirect:/profile";
    }

    /**
     * Adds the inputs to the Spring model so they can be passed to the view. Saves the file to the
     * system in case a new submission is made without changing the file as file inputs cannot be
     * modified in the client so the image must be persisted by the server This is used when the
     * edit profile page needs to be re-rendered after an invalid submission.
     *
     * @param model                  Model to be passed to the view
     * @param firstName              String first name that the user submitted
     * @param lastName               String last name that the user submitted
     * @param email                  String email that the user submitted
     * @param file                   MultipartFile profile picture that the user submitted
     * @param useFailSubmissionImage String boolean value of whether to use the previously saved
     *                               fail submission image
     */
    private void addInputsToModel(Model model, String firstName, String lastName, String email,
            MultipartFile file, String useFailSubmissionImage, String street, String suburb,
            String city, String postcode, String country) {
        User user = userService.getLoggedUser();
        UserDTO userDTO = new UserDTO(firstName, lastName, email, street, suburb, city, postcode,
                country);
        model.addAttribute("userDTO", userDTO);
        if (useFailSubmissionImage.equals("true")) {
            userDTO.setProfileImagePath(user.getFailSubmissionImagePath());
            model.addAttribute("imageToUse", "failSubmissionServerSide");
            model.addAttribute("pfpImageName", user.getFailSubmissionImageOriginalName());
        } else if (!file.isEmpty()) {
            model.addAttribute("contentType", file.getContentType());
            model.addAttribute("pfpImageName", file.getOriginalFilename());
            model.addAttribute("imageToUse", "failSubmissionClientSide");
            try {
                model.addAttribute("pfpImageBase64",
                        Base64.getEncoder().encodeToString(file.getBytes()));
                userService.setUserFailSubmissionImage(file);
            } catch (IOException e) {
                logger.warn(e.getMessage());
            }
        }
    }
}
