package nz.ac.canterbury.seng302.homehelper.controller;

import jakarta.servlet.http.HttpSession;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.service.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MyRenovationsPageController {

    private final RenovationService renovationService;
    private final UserService userService;
    Logger logger = LoggerFactory.getLogger(MyRenovationsPageController.class);

    @Autowired
    public MyRenovationsPageController(RenovationService renovationService,
            UserService userService) {
        this.renovationService = renovationService;
        this.userService = userService;
    }

    /**
     * Maps the /myRenovations URL to myRenovations.html
     *
     * @param model representation of the renovation records for use in thymeleaf
     * @return the String representing the name of the myRenovations html file
     */
    @GetMapping("/myRenovations")
    public String myRenovations(Model model, HttpSession session) {
        session.setAttribute("accessMethod", "myRenovations");
        logger.info("GET /myRenovations");
        model.addAttribute("renovations",
                renovationService.getRenovationRecords(userService.getLoggedUser()));
        return "myRenovationsTemplate";
    }


    /**
     * Delete the renovation record at the given id then redirects back to the myRenovations page
     *
     * @param renovationId the id of the renovation meant to be deleted
     * @return redirect to the myRenovations page
     */
    @PostMapping("deleteRenovation")
    public String deleteRenovation(@RequestParam("renovationId") String renovationId) {
        logger.info("POST /deleteRenovation");
        long longId = Long.parseLong(renovationId);

        RenovationRecord renovation = renovationService.getRenovationRecordById(longId);
        if (!userService.userOwnsRecord(renovation)) {
            throw new SecurityException("You do not have permission to delete this record");
        }
        renovationService.deleteRenovationRecordById(longId);
        return "redirect:/myRenovations";
    }
}
