package nz.ac.canterbury.seng302.homehelper.controller;

import java.util.Objects;
import nz.ac.canterbury.seng302.homehelper.entity.Competition;
import nz.ac.canterbury.seng302.homehelper.entity.CompetitionDesign;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.utils.DesignSortingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CompetitionDetailsController {

    private final UserService userService;
    CompetitionService competitionService;
    Logger logger = LoggerFactory.getLogger(CompetitionDetailsController.class);


    @Autowired
    CompetitionDetailsController(CompetitionService competitionService, UserService userService) {
        this.competitionService = competitionService;
        this.userService = userService;
    }

    /**
     * Maps the /competitionDetails URL to competitionDetails.html and passes the id of the
     * competition so the correct details are loaded.
     *
     * @param model The model attached to the template to store competition info
     * @param id    id of the competition which details should be loaded.
     * @return the String representing the name of the myRenovations html file
     */
    @GetMapping("/competitionDetails/{id}")
    public String competitionDetails(Model model, @PathVariable Long id,
            @RequestParam(required = false) Boolean entering) {
        logger.info("GET /competitionDetails/{}", id);

        Competition competition = competitionService.getCompetitionById(id);
        CompetitionDesign[] top3Entries = competitionService.getTopCompetitionDesignsByCompetition(
                id, 3);

        model.addAttribute("firstPlace", top3Entries[0]);
        model.addAttribute("secondPlace", top3Entries[1]);
        model.addAttribute("thirdPlace", top3Entries[2]);
        model.addAttribute("currentUserEntry",
                competitionService.getCompetitionDesignByCompetitionAndUser(id));
        model.addAttribute("competition", competition);
        model.addAttribute("entering", entering);
        model.addAttribute("previousCompetition", !Objects.equals(competition.getId(),
                competitionService.getCurrentCompetition().getId()));
        model.addAttribute("submittedEntriesCount",
                competitionService.getSubmittedEntriesCount(competition.getId()));
        model.addAttribute("votedDesigns", userService.getLoggedUser().getVotedOnDesigns().stream().map(CompetitionDesign::getId).toList());
        return "competitions/competitionDetails";
    }

    /**
     * Endpoint to receive paginated results for competition entries
     *
     * @param model    The model used to populate the returned fragment
     * @param id       Competition id to retrieve entries for
     * @param pageNum  Index of page to retrieve
     * @param pageSize Number of results per page
     * @return A fragment containing the entry previews, and pagination buttons.
     */
    @GetMapping("/competitionDetails/{id}/paged")
    public String competitionEntriesPage(Model model, @PathVariable Long id,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false, defaultValue = "VOTES_DESC") DesignSortingType sortBy) {
        logger.info("GET /competitionDetails/{}/paged?pageNum={}&pageSize={}&sortBy={}", id,
                pageNum,
                pageSize, sortBy.getSort().toString());

        Page<CompetitionDesign> page = competitionService.getCompetitionDesignsPage(id, pageNum,
                pageSize, sortBy);
        logger.info("Number of designs on page: {}", page.getTotalElements());

        model.addAttribute("designs", page.getContent());
        Competition competition = competitionService.getCompetitionById(id);
        model.addAttribute("previousCompetition", !Objects.equals(competition.getId(),
                competitionService.getCurrentCompetition().getId()));

        model.addAttribute("competition", competition);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("pageNum", pageNum);
        model.addAttribute("votedDesigns", userService.getLoggedUser().getVotedOnDesigns().stream().map(CompetitionDesign::getId).toList());

        CompetitionDesign design = competitionService.getCurrentUserCompetitionDesign();
        if (design != null) {
            model.addAttribute("userDesignId", design.getId());
        }

        return "competitions/competitionEntriesPageable";
    }
}
