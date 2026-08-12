package nz.ac.canterbury.seng302.homehelper.controller;

import java.util.List;
import nz.ac.canterbury.seng302.homehelper.entity.Competition;
import nz.ac.canterbury.seng302.homehelper.entity.CompetitionDesign;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PreviousCompetitionsController {

    Logger logger = LoggerFactory.getLogger(HomeController.class);

    private CompetitionService competitionService;

    public PreviousCompetitionsController(CompetitionService competitionService) {
        this.competitionService = competitionService;
    }

    /**
     * Gets a page fragment of previous competitions displayed in a grid
     *
     * @param model    Model
     * @param pageSize number of results to retrieve per page
     * @param pageNum  index of page to retrieve
     * @return competitions/previousCompetitionsPageable
     */
    @GetMapping("/previous-competitions/paged")
    public String results(
            @RequestParam(required = false, defaultValue = "0") int pageNum,
            @RequestParam(required = false, defaultValue = "4") int pageSize,
            Model model) {
        logger.info("GET /previous-competitions/results/paged?pageNum={}&pageSize={}",
                pageNum, pageSize);
        Page<Competition> page = competitionService.getPageOfPreviousCompetitions(pageNum,
                pageSize);

        // ChatGPT generated code, used to add tuples of (competition, winningDesign) to the model
        List<Object[]> tuples = page.getContent().stream()
                .map(c -> {
                    CompetitionDesign[] topDesigns = competitionService.getTopCompetitionDesignsByCompetition(
                            c.getId(), 1);
                    CompetitionDesign winningDesign =
                            (topDesigns.length > 0) ? topDesigns[0] : null;
                    return new Object[]{c, winningDesign};
                })
                .toList();

        model.addAttribute("competitions", tuples);

        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("pageNum", pageNum);
        return "competitions/previousCompetitionsPageable";
    }
}
