package nz.ac.canterbury.seng302.homehelper.controller;

import jakarta.servlet.http.HttpSession;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.SearchQuery;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MyRenovationsSearchController {

    private final Logger logger = LoggerFactory.getLogger(MyRenovationsSearchController.class);

    private final UserService userService;

    private final RenovationService renovationService;

    @Autowired
    public MyRenovationsSearchController(UserService userService,
            RenovationService renovationService) {
        this.userService = userService;
        this.renovationService = renovationService;
    }

    /**
     * Get mapping for the search renovations view endpoint Adds whether the result is empty or not
     * to the model to be passed into the view. Adds the query to the model so it can be injected
     * into the view
     *
     * @param query String inputted query from the user
     * @param model Spring Model for passing values to the view
     * @return String mapping to the myRenovationsSearch html file
     */
    @GetMapping("/searchMyRenovations")
    public String myRenovationsSearch(@RequestParam(required = false) String query,
            Model model, HttpSession session) {
        SearchQuery searchQuery = (SearchQuery) session.getAttribute(
                "searchMyRenovationsSearchQuery");
        if (searchQuery == null) {
            searchQuery = new SearchQuery();
        }
        if (query != null) {
            searchQuery.setQuery(query);
            searchQuery.setPage(1);
        }

        logger.info("GET /searchMyRenovations?query={}",
                searchQuery.getQuery().replaceAll("[\n\r]",
                        "_")); // The replaceAll is there to prevent logging injections

        User loggedInUser = userService.getLoggedUser();
        boolean emptyResult = renovationService.searchRenovationRecords(searchQuery.getQuery(),
                loggedInUser,
                Integer.MAX_VALUE, 1).isEmpty();

        model.addAttribute("emptyResult", emptyResult);
        session.setAttribute("searchMyRenovationsSearchQuery", searchQuery);
        session.setAttribute("accessMethod", "myRenovationsSearch");
        return "myRenovationsSearch";
    }

    /**
     * Get Mapping for the getMyRenovationSearch endpoint Used in AJAX requests from the client to
     * paginate the renovation results from the query. gets the renovations for the specific page
     * and adds them to the model
     *
     * @param page           the page number
     * @param resultsPerPage the number of requests per page
     * @param query          the search query
     * @param model          Spring Model to handle injecting values into the view
     * @return String mapping to the renovationSearchResults fragment
     */
    @GetMapping("/getMyRenovationsSearch")
    public String getMyRenovationsSearch(@RequestParam int page,
            @RequestParam int resultsPerPage,
            @RequestParam(required = false) String query,
            Model model, HttpSession session) {
        SearchQuery searchQuery = (SearchQuery) session.getAttribute(
                "searchMyRenovationsSearchQuery");
        if (searchQuery == null) {
            searchQuery = new SearchQuery();
        }
        searchQuery.setPage(page);
        searchQuery.setResultsPerPage(resultsPerPage);
        if (query != null) {
            searchQuery.setQuery(query);
        }
        String loggableQuery = searchQuery.getQuery()
                .replaceAll("[\n\r]", "_"); // Prevent log injections
        logger.info("GET /getMyRenovationsSearch?query={}&page={}&count={}", loggableQuery,
                searchQuery.getPage(),
                searchQuery.getResultsPerPage());
        User loggedInUser = userService.getLoggedUser();
        Page<RenovationRecord> searchResults = renovationService.searchRenovationRecords(
                searchQuery.getQuery(),
                loggedInUser, searchQuery.getResultsPerPage(), searchQuery.getPage());
        searchQuery.setTotalPages(searchResults.getTotalPages());
        model.addAttribute("searchResults", searchResults.getContent());
        session.setAttribute("searchMyRenovationsSearchQuery", searchQuery);
        return "fragments/renovationSearchResultsFragment :: renovationSearchResults";
    }
}
