package nz.ac.canterbury.seng302.homehelper.controller;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import nz.ac.canterbury.seng302.homehelper.dto.DesignDataDTO;
import nz.ac.canterbury.seng302.homehelper.dto.FileRetrievalResponse;
import nz.ac.canterbury.seng302.homehelper.entity.CompetitionDesign;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionDesignService;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionService;
import nz.ac.canterbury.seng302.homehelper.service.SceneModelService;
import nz.ac.canterbury.seng302.homehelper.service.SceneTextureService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.utils.DesignValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller class for handling mapping HTTP endpoints related to editing a competition design
 */
@Controller
public class EditCompetitionDesignController {

    private final CompetitionDesignService competitionDesignService;
    private final UserService userService;
    private final SceneModelService sceneModelService;
    private final SceneTextureService sceneTextureService;
    private final CompetitionService competitionService;
    Logger logger = LoggerFactory.getLogger(EditCompetitionDesignController.class);

    @Autowired
    public EditCompetitionDesignController(CompetitionDesignService competitionDesignService,
            UserService userService, SceneModelService sceneModelService,
            SceneTextureService sceneTextureService, CompetitionService competitionService) {
        this.competitionDesignService = competitionDesignService;
        this.userService = userService;
        this.sceneModelService = sceneModelService;
        this.sceneTextureService = sceneTextureService;
        this.competitionService = competitionService;
    }

    /**
     * Retrieves the scene data for the competition entry with the given id
     *
     * @param id the id of the competition design to retrieve the data of
     * @return {@code ResponseEntity<Resource>} containing the data within the entries glb file
     */
    @GetMapping("/getCompetitionEntryData/{id}")
    public ResponseEntity<FileRetrievalResponse> getCompetitionEntryData(@PathVariable Long id) {
        logger.info("GET /getCompetitionEntryData/{}", id);
        try {
            FileRetrievalResponse response = competitionDesignService.getFileRetrievalResponse(id);
            return ResponseEntity.ok().body(response);
        } catch (IOException e) {
            logger.error("Could not retrieve design data for design with id: {}, message: {}",
                    id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (NullPointerException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Uploads the given image as the thumbnail of the competition entry with the given id if it is
     * valid.
     *
     * @param image               thumbnail image to upload
     * @param competitionDesignId id of the competition entry to upload to
     * @return {@code ResponseEntity<String>} representing the success of the request
     */
    @PostMapping("upload/image/competitionEntry/{competitionDesignId}")
    public ResponseEntity<String> saveCompetitionEntryImage(
            @RequestParam("image") MultipartFile image, @PathVariable Long competitionDesignId) {
        logger.info("POST /upload/image/competitionEntry/{}", competitionDesignId);
        return competitionDesignService.validateAndSaveCompetitionEntryImage(
                competitionDesignId, image);
    }

    /**
     * Edits the competition entry. Updates the name and description of the entry.
     *
     * @param competitionEntryId id of the entry to edit
     * @param data               dto with the new name and description of the entry
     * @return {@code ResponseEntity<String>} representing the success of the request
     */
    @PostMapping("editCompetitionEntry/{competitionEntryId}")
    public ResponseEntity<String> editCompetitionEntry(@PathVariable Long competitionEntryId,
            @RequestPart("json") DesignDataDTO data) {
        logger.info("POST /editCompetitionEntry/{}", competitionEntryId);

        String name = data.getName();
        String description = data.getDescription();

        if (!competitionDesignService.userOwnsCompetitionDesign(competitionEntryId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You do not have permission to edit this competition entry");
        }

        Map<String, String> errors = DesignValidator.validateDesignDetails(name, description);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors.values().toArray()[0].toString());
        }

        competitionDesignService.updateCompetitionEntryDetails(name, description,
                competitionEntryId);
        return ResponseEntity.ok().build();
    }

    /**
     * Get Mapping for the edit competition entry page.
     *
     * @param model               Spring model to handle passing attributes to the view
     * @param competitionDesignId id of the competition entry to edit
     * @return {@code String} representing the edit competition design page
     */
    @GetMapping("editCompetitionEntry/{competitionDesignId}")
    public String editCompetitionEntry(Model model, @PathVariable Long competitionDesignId) {
        logger.info("GET /editCompetitionEntry/{}", competitionDesignId);
        User loggedInUser = userService.getLoggedUser();
        CompetitionDesign entry = competitionDesignService.getCompetitionDesignById(
                competitionDesignId);
        if (entry == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Competition entry with id " + competitionDesignId + " not found");
        } else if (!Objects.equals(loggedInUser.getId(), entry.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have permission to edit this competition entry");
        } else if (entry.isSubmitted()) {
            return "redirect:/competitionEntry/" + competitionDesignId;
        }

        model.addAttribute("design", entry);
        model.addAttribute("owned", true);
        model.addAttribute("ownedAndSubmitted", entry.isSubmitted());
        model.addAttribute("ownerId", userService.getLoggedUser().getId());
        model.addAttribute("userModels", sceneModelService.getSceneModelsForUser());
        model.addAttribute("publicModels", sceneModelService.getPublicModels());
        model.addAttribute("textures", sceneTextureService.getPublicTextures());
        model.addAttribute("customTextures", sceneTextureService.getUsersCustomTextures());
        return "editDesign";
    }

    /**
     * POST mapping to submit your competition entry.
     *
     * @param id Competition design entry to submit.
     * @return ResponseEntity
     */
    @PostMapping("submitEntry/{id}")
    public ResponseEntity<String> submitEntry(@PathVariable Long id) {
        if (!competitionDesignService.userOwnsCompetitionDesign(id)) {
            return ResponseEntity.badRequest()
                    .body("You cannot submit a design that is not yours.");
        }

        competitionDesignService.submitCompetitionDesign(id);
        return ResponseEntity.ok("Submitted entry with id: " + id + " to competition.");
    }

    /**
     * An endpoint for deleting the specified entry
     *
     * @param id the id of the entry you want to delete
     * @return a redirect to the competitiondetails page corresponding to the competition the design
     * was deleted from
     */
    @PostMapping("competitionEntry/{id}/delete")
    public String deleteCompetitionEntry(@PathVariable Long id) {
        logger.info("POST /competitionEntry/{}/delete", id);

        // if entry doesn't exist or the user doesn't own 403 forbidden
        if (!competitionDesignService.userOwnsCompetitionDesign(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have permission to delete this competition entry");
        }

        // get the entry by id, we know it exists by this point
        CompetitionDesign design = competitionDesignService.getCompetitionDesignById(id);

        // if the entry is from a past competition then 403 forbidden
        if (!competitionService.getCurrentCompetition().getId()
                .equals(design.getCompetition().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can't delete a competition entry from a past competition");
        }

        // if it has been submitted, 403 forbidden
        if (design.isSubmitted()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can't delete a competition entry that is submitted");
        }

        // validated

        try {
            competitionDesignService.deleteDesign(id);
        } catch (IOException e) {
            logger.warn("IOException deleting competition design with id {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to delete competition entry");
        }
        return String.format("redirect:/competitionDetails/%s", design.getCompetition().getId());

    }
}
