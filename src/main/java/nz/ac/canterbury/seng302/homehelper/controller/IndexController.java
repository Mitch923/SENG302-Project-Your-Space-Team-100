package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * This is a basic spring boot controller, note the @link{Controller} annotation which defines this.
 * This controller defines endpoints as functions with specific HTTP mappings
 */
@Controller
public class IndexController {

    private final UserService userService;
    Logger logger = LoggerFactory.getLogger(IndexController.class);

    public IndexController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Landing page for Your Space<br> Also checks if user is logged in and updated header
     * fragment
     *
     * @param model The model
     * @return thymeleaf landing template
     */
    @GetMapping
    public String landing(Model model) { // Direct to landing page
        logger.info("GET /");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean unverifiedUser = authentication.getAuthorities().stream()
                .anyMatch(
                        grantedAuthority -> grantedAuthority.getAuthority()
                                .equals("ROLE_UNVERIFIED"));
        if (!userService.isLoggedIn() || unverifiedUser) {
            model.addAttribute("loggedIn", false);
        } else {
            model.addAttribute("loggedIn", true);
        }

        return "landing";
    }

}
