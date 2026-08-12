package nz.ac.canterbury.seng302.homehelper.controller;

import jakarta.servlet.http.HttpSession;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.SearchQuery;
import nz.ac.canterbury.seng302.homehelper.service.RenovationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PublicRenovationsController {

    private final RenovationService renovationService;
    Logger logger = LoggerFactory.getLogger(PublicRenovationsController.class);

    @Autowired
    public PublicRenovationsController(RenovationService renovationService) {
        this.renovationService = renovationService;
    }

    /**
     * Maps the /publicRenovations URL to publicRenovationsTemplate.html
     *
     * @param renovationId if present goes to the page of the given renovationId
     * @param session      contains the search query data object which is used elsewhere
     * @param model        representation of the renovation records for use in thymeleaf
     * @return the String representing the name of the publicRenovationsTemplate html file
     */
    @GetMapping("/publicRenovations")
    public String publicRenovations(
            @RequestParam(required = false) String renovationId,
            HttpSession session,
            Model model) {
        SearchQuery searchQuery = (SearchQuery) session.getAttribute("renovationSearchQuery");
        if (searchQuery == null) {
            searchQuery = new SearchQuery();
        }
        searchQuery.setId(0);
        if (renovationId != null) {
            try {
                long id = Long.parseLong(renovationId);
                searchQuery.setId(id);
            } catch (NumberFormatException ignored) {
            }
        }
        session.setAttribute("renovationSearchQuery", searchQuery);
        session.setAttribute("accessMethod", "browseRenovations");
        logger.info("GET /publicRenovations");
        model.addAttribute("renovations",
                renovationService.searchRenovationRecords("", null, 10, 1).getContent());
        return "publicRenovationsTemplate";
    }

    /**
     * Get a page of public renovation record based on page number and size
     *
     * @param session the page number and results per page
     * @return a fragment with all the public renovation records
     */
    @GetMapping("/getPublicRenovations")
    public String getPublicRenovations(
            @RequestParam(required = false) int page,
            @RequestParam(name = "count", required = false) int resultsPerPage,
            HttpSession session, Model model) {
        SearchQuery searchQuery = (SearchQuery) session.getAttribute("renovationSearchQuery");
        if (searchQuery == null) {
            searchQuery = new SearchQuery();
        }
        if (page != 0) {
            searchQuery.setPage(page);
        }
        if (resultsPerPage != 0) {
            searchQuery.setResultsPerPage(resultsPerPage);
        }
        if (searchQuery.getId() != 0) {
            int pageNum = renovationService.findPageNumberOfId(searchQuery.getId(), resultsPerPage,
                    searchQuery.getQuery(), null);
            searchQuery.setPage(pageNum);
            searchQuery.setId(0);
        }
        logger.info("GET /getPublicRenovations/?count={}&page={}", searchQuery.getResultsPerPage(),
                searchQuery.getPage());

        Page<RenovationRecord> publicRenovations = renovationService.searchRenovationRecords("",
                null, searchQuery.getResultsPerPage(), searchQuery.getPage());
        if (!publicRenovations.hasContent() && searchQuery.getPage() != 1) {
            int newPage = 1;
            if (searchQuery.getPage() - 1 == publicRenovations.getTotalPages()) {
                newPage = publicRenovations.getTotalPages();
            }
            searchQuery.setPage(newPage);
            publicRenovations = renovationService.searchRenovationRecords("",
                    null, searchQuery.getResultsPerPage(), searchQuery.getPage());
        }
        model.addAttribute("publicRenovations",
                publicRenovations.getContent()); // -1 to account for 0 based indexing
        searchQuery.setTotalPages(publicRenovations.getTotalPages());
        session.setAttribute("renovationSearchQuery", searchQuery);
        return "fragments/publicRenovationsFragment :: publicRenovations";
    }
}
