package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.entity.Competition;
import nz.ac.canterbury.seng302.homehelper.entity.CompetitionDesign;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/home")
public class HomeController {

    private final CompetitionService competitionService;
    Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    public HomeController(CompetitionService competitionService) {
        this.competitionService = competitionService;
    }

    /**
     * Gets the home page thymeleaf template
     *
     * @return thymeleaf template
     */
    @GetMapping
    public String home(Model model) {
        logger.info("GET /home");

        Competition currentCompetition = competitionService.getCurrentCompetition();
        CompetitionDesign[] top3Designs = competitionService.getTopCompetitionDesignsByCompetition(
                currentCompetition.getId(), 3);

        model.addAttribute("currentUserEntry",
                competitionService.getCurrentUserCompetitionDesign());
        model.addAttribute("weeklyCompetition", currentCompetition);
        model.addAttribute("currentFirst", top3Designs[0]);
        model.addAttribute("currentSecond", top3Designs[1]);
        model.addAttribute("currentThird", top3Designs[2]);
        return "home";
    }
}
