package nz.ac.canterbury.seng302.homehelper.controller;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller class for handling the login page requests
 * <p>
 * Provides one get mapping that adds an error attribute to the model if an error is passed to it
 */
@Controller
@RequestMapping("/login")
public class LoginController {

    UserService userService;
    Logger logger = LoggerFactory.getLogger(LoginController.class);

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Loads Login thymeleaf template. If an error message is passed to it, this is added to the
     * model to be rendered on the page.
     *
     * @param error a String error message
     * @param model the Spring Model used to pass the error message to the view
     * @return String representing the loginTemplate html file
     */
    @GetMapping
    public String login(@RequestParam(value = "error", required = false) String error,
            @RequestParam(required = false) String email,
            Model model) {
        logger.info("GET /login");

        if (userService.isLoggedIn()) {
            return "redirect:/home";
        }

        if (error != null) {
            model.addAttribute("error", "The email address is unknown,"
                    + " or the password is invalid");
        }
        if (email != null) {
            model.addAttribute("email",
                    new String(Base64.getDecoder().decode(email), StandardCharsets.UTF_8));
        }

        return "loginTemplate";
    }

}