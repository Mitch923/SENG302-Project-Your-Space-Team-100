package nz.ac.canterbury.seng302.homehelper.controller;


import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationDesign;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.SearchQuery;
import nz.ac.canterbury.seng302.homehelper.service.RenovationDesignService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.SceneModelService;
import nz.ac.canterbury.seng302.homehelper.service.SceneTextureService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class RecordDisplayPageController {

    private final UserService userService;
    private final RenovationService renovationService;
    private final RenovationDesignService renovationDesignService;
    private final SceneModelService sceneModelService;
    private final SceneTextureService sceneTextureService;
    Logger logger = LoggerFactory.getLogger(RecordDisplayPageController.class);


    @Autowired
    public RecordDisplayPageController(RenovationService renovationService,
            RenovationDesignService renovationDesignService,
            UserService userService,
            SceneModelService sceneModelService,
            SceneTextureService sceneTextureService) {
        this.renovationService = renovationService;
        this.renovationDesignService = renovationDesignService;
        this.userService = userService;
        this.sceneModelService = sceneModelService;
        this.sceneTextureService = sceneTextureService;
    }

    /**
     * Maps the /viewRenovation URL to record-view.html passing the id of the record that is to be
     * displayed on the page as a parameter.
     *
     * @param id    id of the model to be displayed
     * @param model representation of the renovation records for use in thymeleaf
     * @return the String representing the name of the record-display-page html file
     */
    @GetMapping("/viewRenovation/{id}")
    public String viewRenovation(Model model,
            @PathVariable long id,
            @RequestParam(name = "errorMessages", required = false) HashMap<String, String> errorHash,
            @RequestParam(name = "designName", required = false) String designName,
            @RequestParam(name = "designDescription", required = false) String designDescription,
            @RequestParam(value = "roomName", required = false) List<String> roomName,
            @RequestParam(required = false) String designId,
            HttpSession session) {
        RenovationRecord renovationRecord = renovationService.getRenovationRecordById(id);
        if (renovationRecord == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Renovation record with id " + id + " not found");
        } else if (!userService.userOwnsRecord(renovationRecord)
                && !renovationRecord.isPublicRecord()) {
            throw new SecurityException("You do not have permission to view this record");
        }

        logger.info("GET /viewRenovation/{}", id);
        SearchQuery searchQuery = (SearchQuery) session.getAttribute("designSearchQuery");
        if (searchQuery == null) {
            searchQuery = new SearchQuery(0);
        }
        if (designId != null) {
            try {
                long designId_ = Long.parseLong(designId);
                int pageNum = renovationDesignService.findPageNumberOfId(designId_,
                        searchQuery.getResultsPerPage(),
                        id);
                searchQuery.setPage(pageNum);
            } catch (NumberFormatException ignored) {
            }
        }
        searchQuery.resetIfNotMatches(renovationRecord.getId());
        session.setAttribute("designSearchQuery", searchQuery);

        model.addAttribute("tags", renovationRecord.getTags());
        model.addAttribute("owned", userService.userOwnsRecord(renovationRecord));
        model.addAttribute("renovationRecord", renovationRecord);
        model.addAttribute("rooms", renovationRecord.getRooms());
        model.addAttribute("designName", designName);
        model.addAttribute("designDescription", designDescription);
        model.addAttribute("roomNames", roomName);
        if (renovationRecord.getRenovationLocation() != null) {
            if (!userService.userOwnsRecord(renovationRecord)) {
                String address = renovationRecord.getRenovationLocation().getCity() + ", "
                        + renovationRecord.getRenovationLocation().getCountry();
                model.addAttribute("locationFullAddress", address);
            } else {
                model.addAttribute("locationFullAddress",
                        renovationRecord.getRenovationLocation().getFullAddress());
            }
        }
        return "record-view";
    }

    /**
     * Gets the relevant design view for the view renovation page based on the page number and
     * number per page
     *
     * @param id    the id of the renovation
     * @param model the model
     * @return the populated renovation designs fragment
     */
    @GetMapping("/viewRenovation/{id}/getDesigns")
    public String getDesigns(@PathVariable long id,
            @RequestParam int page,
            @RequestParam int resultsPerPage,
            Model model,
            HttpSession session) {
        SearchQuery searchQuery = (SearchQuery) session.getAttribute("designSearchQuery");
        if (searchQuery == null) {
            searchQuery = new SearchQuery();
        }
        searchQuery.setPage(page);
        searchQuery.setResultsPerPage(resultsPerPage);
        logger.info("GET /viewRenovation/{}/getDesigns", id);
        RenovationRecord renovationRecord = renovationService.getRenovationRecordById(id);
        if (!userService.userOwnsRecord(renovationRecord) && !renovationRecord.isPublicRecord()) {
            throw new SecurityException("You do not have permission to view this design");
        }
        // a page object which holds a page of designs along with the total count of designs
        Page<RenovationDesign> designPage = renovationDesignService.getDesignPageForRenovation(
                renovationRecord,
                searchQuery.getPage(),
                searchQuery.getResultsPerPage());

        if (!designPage.hasContent() && searchQuery.getPage() != 1) {
            int newPage = 1;
            if (searchQuery.getPage() - 1 == designPage.getTotalPages()) {
                newPage = designPage.getTotalPages();
            }
            searchQuery.setPage(newPage);
            designPage = renovationDesignService.getDesignPageForRenovation(renovationRecord,
                    newPage,
                    searchQuery.getResultsPerPage());
        }
        int totalPages = designPage.getTotalPages();
        searchQuery.setTotalPages(totalPages);
        model.addAttribute("owned", userService.userOwnsRecord(renovationRecord));
        model.addAttribute("id", id);
        model.addAttribute("renovationRecord", renovationRecord);
        model.addAttribute("renovationDesigns", designPage.getContent());
        // total pages relative to what design element count was specified
        session.setAttribute("designSearchQuery", searchQuery);
        return "fragments/renovationDesigns :: renovationDesigns";

    }

    /**
     * Post request endpoint for updating the visibility property of a renovation record without
     * reloading the entire page.
     *
     * @param id         Id of the renovation to update
     * @param visibility Boolean value for whether the renovation is public or not.
     * @return A status code to indicate success
     */
    @PostMapping("/viewRenovation/{id}/setVisibility")
    @ResponseBody
    public ResponseEntity<Void> setVisibility(@PathVariable("id") String id,
            @RequestParam("visibility") boolean visibility) {
        if (!userService.userOwnsRecord(
                renovationService.getRenovationRecordById(Long.parseLong(id)))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not own this renovation record");
        }
        logger.info("Changing renovation visibility to {} for renovation {}", visibility, id);
        renovationService.setVisibility(Long.parseLong(id), visibility);
        return ResponseEntity.ok().build();
    }


    /**
     * Calls to design service to update a specific designs icon
     *
     * @param id   Target Design
     * @param body Body of the response containing the icon name
     * @return Ok Response entity
     */
    @PostMapping("/viewRenovation/updateIcon/{id}")
    public ResponseEntity<?> updateDesignIcon(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> body
    ) {
        logger.info("POST /viewRenovation/updateIcon/{}", id);
        String iconName = body.get("iconName");
        this.renovationDesignService.updateDesignIcon(id, iconName);
        return ResponseEntity.ok("Icon Updated Successfully");
    }

    @GetMapping("/viewRenovation/{id}/designs")
    public String viewRenovation(@PathVariable long id, Model model) {
        RenovationRecord renovation = renovationService.getRenovationRecordById(id);
        if (!userService.userOwnsRecord(renovation) && !renovation.isPublicRecord()) {
            throw new SecurityException("You do not have permission to view this record");
        }
        model.addAttribute("owned", userService.userOwnsRecord(renovation));
        model.addAttribute("renovationRecord", renovation);
        model.addAttribute("rooms", renovation.getRooms());
        model.addAttribute("designs", renovation.getDesignsForRenovation());
        return "designs-view";
    }

    /**
     * Creates a default design that can be further edited by the user
     *
     * @param model        thyme leaf model
     * @param renovationId id of the renovation the design should be created for
     * @return createDesign template
     */
    @PostMapping("renovationRecord/{renovationId}/createDesign")
    public String createDesign(Model model, @PathVariable long renovationId) {
        logger.info("GET renovationRecord/{}/createDesign", renovationId);
        RenovationRecord renovationRecord = renovationService.getRenovationRecordById(renovationId);
        if (!userService.userOwnsRecord(renovationRecord)) {
            throw new SecurityException(
                    "You do not have permission to make designs for this renovation record");
        }
        RenovationDesign renovationDesign = renovationDesignService.createDesign(
                new RenovationDesign("Untitled Design", "", renovationRecord));
        return "redirect:/renovationRecord/" + renovationId + "/editDesign/"
                + renovationDesign.getId();
    }

    /**
     * Get mapping for the edit design page.
     *
     * @param model        Spring model for passing data to the view
     * @param renovationId the id of the renovation that the design is on
     * @param designId     the id of the design to be edited
     * @return {@code String} representation of the edit design page view
     */
    @GetMapping("renovationRecord/{renovationId}/editDesign/{designId}")
    public String editDesign(Model model, @PathVariable long renovationId,
            @PathVariable long designId) {
        logger.info("GET renovationRecord/{}/editDesign/{}", renovationId, designId);
        if (userService.userOwnsRecord(renovationService.getRenovationRecordById(renovationId))) {
            model.addAttribute("owned", true);
            model.addAttribute("ownerId", userService.getLoggedUser().getId());
        } else {
            model.addAttribute("owned", false);
        }
        RenovationDesign renovationDesign = renovationDesignService.getDesignById(designId);
        model.addAttribute("renovationRecordId", renovationId);
        model.addAttribute("design", renovationDesign);
        model.addAttribute("userModels", sceneModelService.getSceneModelsForUser());
        model.addAttribute("publicModels", sceneModelService.getPublicModels());
        model.addAttribute("textures", sceneTextureService.getPublicTextures());
        model.addAttribute("customTextures", sceneTextureService.getUsersCustomTextures());
        return "editDesign";
    }

}
