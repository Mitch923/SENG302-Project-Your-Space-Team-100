package nz.ac.canterbury.seng302.homehelper.controller;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import java.util.HashMap;
import java.util.Map;
import nz.ac.canterbury.seng302.homehelper.dto.UserDTO;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.Token;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.EmailVerificationService;
import nz.ac.canterbury.seng302.homehelper.service.SpringEmailService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.utils.LocationValidator;
import nz.ac.canterbury.seng302.homehelper.utils.UserDataValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

// ChatGPT generated javaDoc

/**
 * Controller class for handling user registration functionality. It manages the process of
 * displaying the registration form, validating user input, registering a new user, and sending a
 * confirmation email. It also handles user authentication after successful registration.
 * <p>
 * This class supports both GET and POST requests to the "/register" endpoint and integrates with
 * the user service, email verification, and authentication providers.
 */
@Controller
@EnableAsync
@RequestMapping("/register")
public class RegisterController {

    private final SpringEmailService springEmailService;

    private final UserService userService;

    private final EmailVerificationService emailVerificationService;

    Logger logger = LoggerFactory.getLogger(RegisterController.class);

    @Autowired
    public RegisterController(UserService userService,
            EmailVerificationService emailVerificationService,
            SpringEmailService springEmailService) {
        this.userService = userService;
        this.emailVerificationService = emailVerificationService;
        this.springEmailService = springEmailService;
    }

    /**
     * Gets the register page thymeleaf template
     *
     * @return thymeleaf template
     */
    @GetMapping
    public String register(Model model) {
        logger.info("GET /register");
        if (userService.isLoggedIn()) {
            return "redirect:/home";
        }
        UserDTO userDTO = new UserDTO();
        model.addAttribute("userDTO", userDTO);
        return "registerPage";
    }

    // ChatGPT generated javaDoc

    /**
     * Handles POST requests for user registration. This method validates the provided registration
     * details, creates a new user, sends a confirmation email, schedules user deletion (until
     * validated), and authenticates the user. If validation fails, it returns the registration page
     * with error messages. If successful, the user is redirected to their profile page.
     *
     * @param firstName       The user's first name
     * @param lastName        The user's last name
     * @param email           The user's email address
     * @param password        The user's chosen password
     * @param passwordConfirm The confirmation of the password entered
     * @param street          street part of the location
     * @param suburb          suburb part of the location
     * @param city            city part of the location
     * @param postcode        postcode part of the location
     * @param country         country part of the location
     * @param model           The model to carry attributes for the view
     * @return The name of the Thymeleaf template to render
     */
    @PostMapping
    public String register(@RequestParam(name = "firstname") String firstName,
            @RequestParam(name = "lastname") String lastName,
            @RequestParam(name = "email") String email,
            @RequestParam(name = "password") String password,
            @RequestParam(name = "confirm") String passwordConfirm,
            @RequestParam(name = "streetAddress", required = false, defaultValue = "") String street,
            @RequestParam(name = "suburb", required = false, defaultValue = "") String suburb,
            @RequestParam(name = "city", required = false, defaultValue = "") String city,
            @RequestParam(name = "postcode", required = false, defaultValue = "") String postcode,
            @RequestParam(name = "country", required = false, defaultValue = "") String country,
            Model model) {
        logger.info("POST /register");
        // validate inputs
        UserDTO userDTO = new UserDTO(
                firstName,
                lastName,
                password,
                passwordConfirm,
                email,
                street,
                suburb,
                city,
                postcode,
                country);
        Map<String, String> errors = UserDataValidator.validateRegistration(userDTO,
                userService);
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

        model.addAttribute("errors", errors);
        logger.info("errors: {}", errors);

        // return form data to populate registration fields
        model.addAttribute("userDTO", userDTO);
        if (isEmpty(errors)) {
            try {
                User user = userService.registerUser(email.toLowerCase(), password, firstName,
                        lastName);

                userService.setUserLocation(user, location);

                // Asynchronously send the email on another thread to stop slow page load
                logger.info("Sending confirmation email");
                Token newToken = new Token(user);
                userService.saveToken(newToken);
                String generatedCode = newToken.getToken();
                springEmailService.sendSignUpEmail(firstName, email, generatedCode);

                // Schedule user for deletion
                emailVerificationService.scheduleUserDeletion(user);

                logger.info("Redirect to /verification");
                return "redirect:/verification?userId=" + user.getId();

            } catch (UsernameNotFoundException e) {
                logger.error(e.getMessage());
                return "registerPage";
            }
        }
        return "registerPage";
    }

    /**
     * End point to check if an email address is already associated with a user. This is used
     * along with front end validation so the user is alerted if the email is already in use
     * before submitting the form.
     *
     * @param email String - the email address to check.
     * @return Json response body - eg: { emailInUse: "true" }
     */
    @GetMapping("/emailInUse")
    @ResponseBody
    public Map<String, String> emailInUse(@RequestParam String email) {
        Map<String, String> errors = new HashMap<>();
        try {
            User currentUser = userService.getLoggedUser();
            UserDataValidator.validateEmail(errors, email, userService, currentUser.getEmail());
        } catch (UsernameNotFoundException e) {
            UserDataValidator.validateEmail(errors, email, userService, null);
        }
        return Map.of("emailInUse", Boolean.toString(errors.containsKey("emailInUse")));
    }
}