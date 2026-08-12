package nz.ac.canterbury.seng302.homehelper.controller;

import java.util.HashMap;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.EmailVerificationService;
import nz.ac.canterbury.seng302.homehelper.service.SpringEmailService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.utils.UserDataValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reset")
public class LostPasswordController {

    private final UserService userService;
    private final SpringEmailService springEmailService;
    private final EmailVerificationService emailVerificationService;
    Logger logger = LoggerFactory.getLogger(LostPasswordController.class);

    public LostPasswordController(SpringEmailService springEmailService,
            EmailVerificationService emailVerificationService, UserService userService) {
        this.userService = userService;
        this.springEmailService = springEmailService;
        this.emailVerificationService = emailVerificationService;
    }

    /**
     * Loads the reset password page if a reset password token is present and valid. If the token is
     * invalid, redirects to the login page. Otherwise, loads the lostPassword page
     *
     * @param token String reset password token
     * @return String representation of the view
     */
    @GetMapping
    public String reset(@RequestParam(value = "token", required = false) String token, Model model,
            RedirectAttributes redirectAttributes) {
        HashMap<String, String> errors = new HashMap<>();
        logger.info("GET /reset");
        if (userService.isLoggedIn()) {
            return "redirect:/home";
        }
        if (token != null && !token.isEmpty()) {
            userService.validateResetPasswordToken(errors, token);

            if (errors.containsKey("token")) {
                redirectAttributes.addFlashAttribute("error", errors.get("token"));
                return "redirect:/login";
            }
            User userToReset = userService.getUserByResetPasswordToken(token).get();
            String firstName = userToReset.getFirstName();
            String lastName = userToReset.getLastName();
            String email = userToReset.getEmail();

            model.addAttribute("firstName", firstName);
            model.addAttribute("lastName", lastName);
            model.addAttribute("email", email);
            return "resetPassword";
        }

        return "lostPassword";
    }

    /**
     * Handles POST requests to reset password via an email link OR reset the password given the
     * reset password token is in the URL
     *
     * @param email    String email to send the link to
     * @param password String new password entered by the user
     * @param confirm  String confirmed password entered by the user
     * @param token    String unique reset password token taken from the link
     * @param model    Spring Model to handle passing information to the view
     * @return String representation of the view - either the lostPassword page, resetPassword page,
     * or a redirect to login
     */
    @PostMapping()
    public String handleReset(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String confirm,
            @RequestParam(required = false) String token,
            Model model,
            RedirectAttributes redirectAttributes) {
        logger.info("POST /reset");

        HashMap<String, String> errors = new HashMap<>();
        // Handles if the user is on the email submission
        if (email != null) {
            UserDataValidator.validateEmailFormat(errors, email);
            if (errors.isEmpty()) {
                if (userService.existsByEmail(email)) {
                    User userToSend = userService.getUserByEmail(email).get();
                    userService.generateResetPasswordTokenForUser(userToSend);
                    springEmailService.sendResetPasswordEmail(email);
                    emailVerificationService.scheduleResetPasswordTokenRevocation(
                            userToSend.getResetPasswordToken());
                }
                model.addAttribute("success",
                        "An email was sent to the address if it was recognised");
            } else {
                model.addAttribute("email", email);
                model.addAttribute("errors", errors);
            }
            return "lostPassword";
        }

        // Handles the password & password confirm submission
        else if (token != null && !token.isEmpty()) {
            userService.validateResetPasswordToken(errors, token);
            if (errors.containsKey("token")) {
                redirectAttributes.addFlashAttribute("error", errors.get("token"));
                return "redirect:/login";
            }

            User userToReset = userService.getUserByResetPasswordToken(token).get();
            UserDataValidator.validateLostPassword(errors, password, confirm, userToReset);
            if (errors.isEmpty()) {
                // Reset the password and expire the token
                if (userToReset != null) {
                    userService.revokeResetPasswordToken(token);
                    userService.resetPassword(userToReset, password);
                    springEmailService.sendPasswordChangeEmail(userToReset.getFirstName(),
                            userToReset.getEmail());
                    return "redirect:/login";
                }
            } else {
                model.addAttribute("errors", errors);
                return "resetPassword";
            }
        }
        return "lostPassword";
    }
}