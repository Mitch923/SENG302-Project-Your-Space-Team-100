package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.EmailVerificationService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for the verification page.
 */
@Controller
@RequestMapping("/verification")
public class VerificationController {

    private final UserService userService;
    private final EmailVerificationService emailVerificationService;
    Logger logger = LoggerFactory.getLogger(VerificationController.class);

    public VerificationController(UserService userService,
            EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
        this.userService = userService;
    }

    /**
     * Get Verification page
     *
     * @return thymeleaf template, verificationTemplate
     */
    @GetMapping
    public String verification(@RequestParam() Long userId, Model model) {
        model.addAttribute("userId", userId);
        if (userId != null && userService.getUserById(userId).isPresent()) {
            model.addAttribute("userEmail",
                    userService.getUserById(userId).get().getEmail());
        }
        if (userService.getUserById(userId).isPresent() && userService.getUserById(userId).get()
                .getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_USER"))) {
            return "redirect:/login";
        }
        logger.info("GET /verification");
        return "verificationPage";
    }

    /**
     * Implements the logic to check if the code entered is correct and then upgrades the users'
     * role accordingly so that they are permitted to log in.<br> Cancels the deletion of the user
     * if the correct code is entered.
     *
     * @param verificationCode   code input by the user
     * @param userId             The user id of the user to be verified
     * @param model              Model
     * @param redirectAttributes to send error/success information
     * @return if successful, redirect to /login
     */
    @PostMapping
    public String verifyCode(@RequestParam(name = "verificationCode") String verificationCode,
            @RequestParam(name = "userId") String userId, Model model,
            RedirectAttributes redirectAttributes) {
        model.addAttribute("userId", userId);
        if (userId != null && userService.getUserById(Long.parseLong(userId)).isPresent()) {
            model.addAttribute("userEmail",
                    userService.getUserById(Long.parseLong(userId)).get().getEmail());
        }
        logger.info("POST /verification");
        if (userId == null) {
            return "redirect:/login";
        }
        long id = Long.parseLong(userId);
        if (userService.getUserById(id).isEmpty()) {
            model.addAttribute("error", "Signup code invalid");
            redirectAttributes.addFlashAttribute("error",
                    "Signup code invalid");
            redirectAttributes.addFlashAttribute("code", verificationCode);
            return "redirect:/verification?userId=" + userId;
        }
        User user = userService.getUserById(id).get();
        model.addAttribute("user", user);
        boolean correctCode = emailVerificationService.ensureValidCode(verificationCode, id);
        if (correctCode) {
            userService.verifyUser(user);
            emailVerificationService.cancelUserDeletion(user);
            logger.info("User verified, redirecting to login page");
            redirectAttributes.addFlashAttribute("successfulVerification",
                    "Your account has been activated, please log in");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("error",
                    "Signup code invalid");
            redirectAttributes.addFlashAttribute("code", verificationCode);
            return "redirect:/verification?userId=" + userId;
        }
    }


}
