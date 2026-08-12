package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Defines a set of "Controller Global" methods
 */
@ControllerAdvice
public class ControllerAdvisor {

    @Autowired
    private UserService userService;

    // ChatGPT Generated javadoc

    /**
     * Adds the profile image path for the logged-in user to the model.
     * <p>
     * If the user is logged in and has a profile image then the file path for this is returned.
     * Otherwise returns the filepath for the default image.
     * </p>
     *
     * @return the file path of the user's profile image
     */
    @ModelAttribute(name = "profileImagePath")
    public String addUserProfile() {
        if (userService.isLoggedIn()) {
            String filePath = userService.getLoggedUser().getProfileImagePath();

            // Ensure user has uploaded a profile picture
            if (filePath != null) {
                return filePath;
            }
        }

        return "/img/user_profile_images/Default_pfp.svg";
    }

    @ModelAttribute(name = "defaultImagePath")
    public String addDefaultProfile() {
        return "/img/user_profile_images/Default_pfp.svg";
    }
}
