package nz.ac.canterbury.seng302.homehelper.controller;

import java.io.IOException;
import java.util.List;
import nz.ac.canterbury.seng302.homehelper.dto.RenovationAutocompleteDTO;
import nz.ac.canterbury.seng302.homehelper.entity.Competition;
import nz.ac.canterbury.seng302.homehelper.entity.CompetitionDesign;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationDesign;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionDesignService;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationDesignService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller for the import design page when adding a design entry to a competition.
 */
@Controller
@RequestMapping("importDesign")
public class ImportDesignController {

    private final RenovationDesignService renovationDesignService;
    private final CompetitionDesignService competitionDesignService;
    private final CompetitionService competitionService;
    private final RenovationService renovationService;
    Logger logger = LoggerFactory.getLogger(ImportDesignController.class);


    @Autowired
    public ImportDesignController(RenovationDesignService designService,
            CompetitionDesignService competitionDesignService,
            CompetitionService competitionService, RenovationService renovationService) {
        this.renovationDesignService = designService;
        this.competitionDesignService = competitionDesignService;
        this.competitionService = competitionService;
        this.renovationService = renovationService;
    }

    /**
     * Gets the import design page thymeleaf template.
     *
     * @param model Model
     * @return importDesign template.
     */
    @GetMapping()
    public String viewDesignsForImport(Model model) {
        logger.info("GET /importDesign");

        model.addAttribute("competitionTheme",
                competitionService.getCurrentCompetition().getTheme());
        return "competitions/importDesign";
    }

    /**
     * Searches for Renovation Designs with the search query and returns a fragment of the results.
     *
     * @param searchQuery search query (design name)
     * @param renovations the list of renovation ids to filter results by
     * @param model       Model
     * @param pageSize    number of results to retrieve per page
     * @param pageNum     index of page to retrieve
     * @return competitions/searchResultsFragment
     */
    @GetMapping("/search/results/paged")
    public String searchResults(
            @RequestParam(required = false, defaultValue = "0") int pageNum,
            @RequestParam(required = false, defaultValue = "8") int pageSize,
            @RequestParam(required = false, defaultValue = "") String searchQuery,
            @RequestParam(required = false, defaultValue = "") List<Long> renovations,
            Model model
    ) {
        logger.info(
                "GET /importDesign/search/results/paged?searchQuery={}&pageNum={}&pageSize={}&renovations={}",
                searchQuery, pageNum, pageSize, renovations);
        Page<RenovationDesign> page = renovationDesignService.searchUsersRenovationDesigns(
                searchQuery,
                pageNum,
                pageSize,
                renovations
        );

        model.addAttribute("designs", page.getContent());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("pageNum", pageNum);
        model.addAttribute("searchQuery", searchQuery);
        return "competitions/searchResultsFragment";
    }


    /**
     * Creates a duplicate competition design from the given renovation design
     *
     * @param designId the id of the renovation to be duplicated
     * @return the endpoint for editing the new design
     */
    @PostMapping("/{designId}")
    public String importDesign(
            @PathVariable("designId") long designId, Model model) {
        logger.info("POST /importDesign/{}", designId);

        Competition currentCompetition = competitionService.getCurrentCompetition();
        RenovationDesign design = renovationDesignService.getDesignById(designId);
        CompetitionDesign competitionDesign;

        if (!competitionService.validateImportDesign(designId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You cannot import a design if you do not own the design or if you are already entered in the current competition");
        }

        try {
            competitionDesign = competitionDesignService.duplicateRenovationDesign(design,
                    currentCompetition);
        } catch (IOException e) {
            logger.warn("IOException while trying to import design id {}", designId, e);
            return "redirect:/importDesign";
        }
        model.addAttribute("designId", competitionDesign.getId());
        return "redirect:/editCompetitionEntry/" + competitionDesign.getId();
    }

    /**
     * Get the renovation record autocomplete results for dropdown while filtering.
     *
     * @param query String for the renovation record name
     * @return ResponseEntity<List < RenovationRecord>>
     */
    @GetMapping("/renovation/autocomplete")
    public ResponseEntity<List<RenovationAutocompleteDTO>> renovationRecordAutocompleteResults(
            @RequestParam(required = false) String query) {
        logger.info("GET /importDesign/autocomplete?query={}", query);
        List<RenovationRecord> renovationResults = renovationService.getRenovationRecordsByNameSubstring(
                query.trim());
        List<RenovationAutocompleteDTO> results = renovationResults.stream()
                .map(reno -> new RenovationAutocompleteDTO(reno.getId(), reno.getName())).toList();
        return ResponseEntity.ok(results);
    }

}
