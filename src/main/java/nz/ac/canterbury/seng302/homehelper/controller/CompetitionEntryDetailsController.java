package nz.ac.canterbury.seng302.homehelper.controller;

import java.util.Objects;
import nz.ac.canterbury.seng302.homehelper.entity.CompetitionDesign;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionDesignService;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller class for handling serving the view page of a competition entry
 */
@Controller
public class CompetitionEntryDetailsController {

    private final CompetitionDesignService competitionDesignService;
    private final CompetitionService competitionService;
    private final Logger logger = LoggerFactory.getLogger(CompetitionEntryDetailsController.class);
    private final UserService userService;

    public CompetitionEntryDetailsController(CompetitionDesignService competitionDesignService,
            CompetitionService competitionService,
            UserService userService) {
        this.competitionDesignService = competitionDesignService;
        this.competitionService = competitionService;
        this.userService = userService;
    }

    /**
     * Get mapping for the competition entry details page
     *
     * @param id    of the competition entry to view
     * @param model Spring Model to handle passing attributes to the view
     * @return String representing the editDesign html template
     */
    @GetMapping("/competitionEntry/{id}")
    public String getCompetitionEntry(@PathVariable Long id, Model model) {
        logger.info("GET competitionEntry/{}", id);

        CompetitionDesign competitionDesign = competitionDesignService.getCompetitionDesignById(id);
        if (competitionDesign == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        boolean owned = userService.getLoggedUser().getId()
                .equals(competitionDesign.getUser().getId());

        if (owned && !competitionDesign.isSubmitted() && competitionDesign.getCompetition().getId()
                .equals(competitionService.getCurrentCompetition().getId())) {
            return "redirect:/editCompetitionEntry/" + id;
        }
        model.addAttribute("owned", false);
        model.addAttribute("ownedAndSubmitted", owned && competitionDesign.isSubmitted());
        model.addAttribute("design", competitionDesign);
        model.addAttribute("publicEntryView", true);
        model.addAttribute("competitionDesign", true);
        model.addAttribute("previousCompetition",
                !Objects.equals(competitionDesign.getCompetition().getId(),
                        competitionService.getCurrentCompetition().getId()));
        model.addAttribute("previousCompetition",
                !Objects.equals(competitionDesign.getCompetition().getId(),
                        competitionService.getCurrentCompetition().getId()));
        model.addAttribute("votedOn",
                userService.getLoggedUser().getVotedOnDesigns().contains(competitionDesign));
        model.addAttribute("voteCount",
                competitionDesignService.getCompetitionDesignById(id).getVoteCount());
        return "editDesign";
    }

    /**
     * Toggles the vote on the particular competition
     *
     * @param id the id of the competition entry you want to vote on
     */
    @PostMapping("competitionEntry/{id}/vote")
    @ResponseBody
    public void voteForCompetitionEntry(@PathVariable Long id) {
        logger.info("POST competitionEntry/{}/vote", id);
        // check user is logged in
        if (userService.getLoggedUser() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        // check the entry exists
        if (!competitionService.entryExists(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        competitionService.toggleVote(id);
    }
}