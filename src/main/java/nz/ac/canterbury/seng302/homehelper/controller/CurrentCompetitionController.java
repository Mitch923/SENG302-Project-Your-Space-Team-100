package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.entity.CompetitionDesign;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller class for mapping HTTP requests to their respective functions in relation to the
 * current competition
 */
@Controller
public class CurrentCompetitionController {

    private final CompetitionService competitionService;
    Logger logger = LoggerFactory.getLogger(CurrentCompetitionController.class);


    @Autowired
    public CurrentCompetitionController(CompetitionService competitionService) {
        this.competitionService = competitionService;
    }

    /**
     * Creates a new competition entry for the logged-in user in the current weekly competition.
     * Redirects the user to the edit competition entry page for the new competition.
     *
     * @return {@code String} redirecting to the edit competition entry page
     */
    @PostMapping("createCompetitionEntry")
    public String createCompetitionEntry() {
        logger.info("POST /createCompetitionEntry");
        CompetitionDesign newEntry = competitionService.createNewCompetitionEntry();
        if (newEntry == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only have one competition entry");
        }
        return "redirect:/editCompetitionEntry/" + newEntry.getId();
    }

    /**
     * Toggles the user's vote for the design with the given id
     *
     * @param id of the competition entry to toggle the vote for
     * @return http response
     */
    @PostMapping("toggleEntryVote/{id}")
    public ResponseEntity<String> voteForEntry(@PathVariable("id") Long id) {
        if (competitionService.validateToggleVote(id)) {
            competitionService.toggleVote(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }
}
