package nz.ac.canterbury.seng302.homehelper.controller;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import nz.ac.canterbury.seng302.homehelper.dto.DesignDataDTO;
import nz.ac.canterbury.seng302.homehelper.dto.FileRetrievalResponse;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationDesign;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Room;
import nz.ac.canterbury.seng302.homehelper.entity.SceneModel;
import nz.ac.canterbury.seng302.homehelper.entity.SceneTexture;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.SceneTextureRepository;
import nz.ac.canterbury.seng302.homehelper.service.RenovationDesignService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.RoomService;
import nz.ac.canterbury.seng302.homehelper.service.SceneModelService;
import nz.ac.canterbury.seng302.homehelper.service.SceneTextureService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.utils.DesignValidator;
import nz.ac.canterbury.seng302.homehelper.utils.ImageUploadValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;


/**
 * Controller class to handle any functionality for editing designs
 */
@Controller
public class EditRenovationDesignController {

    private final RenovationDesignService renovationDesignService;
    private final RenovationService renovationService;
    private final UserService userService;
    private final RoomService roomService;
    private final SceneTextureService sceneTextureService;
    private final SceneModelService sceneModelService;
    private final SceneTextureRepository sceneTextureRepository;
    Logger logger = LoggerFactory.getLogger(EditRenovationDesignController.class);

    @Autowired
    EditRenovationDesignController(RenovationDesignService renovationDesignService,
            RenovationService renovationService,
            UserService userService, RoomService roomService,
            SceneModelService sceneModelService,
            SceneTextureService sceneTextureService,
            SceneTextureRepository sceneTextureRepository) {
        this.renovationDesignService = renovationDesignService;
        this.renovationService = renovationService;
        this.userService = userService;
        this.roomService = roomService;
        this.sceneTextureService = sceneTextureService;
        this.sceneTextureRepository = sceneTextureRepository;
        this.sceneModelService = sceneModelService;
    }

    /**
     * Post endpoint for uploading a custom model and saving the model content and details
     *
     * @param userId      Id of the user uploading the model
     * @param displayName name of the model
     * @param modelGLB    GLB file that contains the model data and textures
     * @return http status and if successful the id of the model when it is saved
     */
    @PostMapping("upload/model/{userId}")
    public ResponseEntity<String> uploadModel(
            @PathVariable("userId") long userId,
            @RequestParam("displayName") String displayName,
            @RequestPart("modelGLB") MultipartFile modelGLB
    ) {

        if (userService.getLoggedUser().getId() != userId) {
            logger.info("User {} is not logged in", userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You do not have permission to upload a model for this user");
        }

        // we know the logged-in user exists
        User user = userService.getUserById(userId).get();

        // validate file
        Map<String, String> fileErrors = DesignValidator.validateModelFile(modelGLB);
        for (String error : fileErrors.keySet()) {
            return ResponseEntity.badRequest().body(fileErrors.get(error));
        }

        logger.info("Saving model with name: {}", displayName);

        try {
            // Step 1: Create and save the SceneModel entity
            SceneModel sceneModel = new SceneModel(displayName, user, "", "");
            sceneModelService.saveModelDetails(sceneModel);

            // Step 2: Save the actual GLB file data to disk
            sceneModelService.saveModelData(sceneModel.getId(), modelGLB.getBytes());

            return ResponseEntity.ok(Long.toString(sceneModel.getId()));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("An error occurred while uploading the model");
        }
    }

    /**
     * Uploads a parallel texture for the custom model with the given id
     *
     * @param parallelTexture texture file to upload
     * @param modelId         of the model to upload for
     * @return HTTP response entity indicating the success of the request
     */
    @PostMapping("upload/parallelTexture/{modelId}")
    public ResponseEntity<Object> uploadParallelTexture(
            @RequestPart("parallelTexture") MultipartFile parallelTexture,
            @PathVariable("modelId") Long modelId
    ) {
        return sceneModelService.saveParallelTexture(modelId, parallelTexture);
    }

    /**
     * Saves the design details + data and responds with a success message or the appropriate
     * failure message.
     *
     * @param renovationId id of the renovation the design is for
     * @param designId     id of the design to save
     * @param data         the json object in the shape of DesignDataDTO that holds the name and
     *                     description to be saved
     * @return OK or Server error response.
     */
    @PostMapping("renovationRecord/{renovationId}/saveDesign/{designId}")
    public ResponseEntity<String> saveDesign(
            @PathVariable("renovationId") long renovationId,
            @PathVariable long designId,
            @RequestPart("json") DesignDataDTO data
    ) {
        logger.info("POST request to save design with id: {}", designId);
        // Access JSON fields
        String name = data.getName();
        String description = data.getDescription();
        String designRoomId = data.getDesignRoomId();
        Long roomId = null;

        if (!userService.userOwnsRecord(renovationService.getRenovationRecordById(renovationId))) {
            throw new SecurityException("You do not have permission to edit this design");
        }

        // validate the design details
        Map<String, String> errors = DesignValidator.validateDesignDetails(name, description);
        if (!errors.isEmpty()) {
            // send the first error string that exists
            return ResponseEntity.badRequest().body(errors.values().toArray()[0].toString());
        }

        try {
            // if the designRoomId value is null or default then keep roomId null, so that the room is set to none on save
            if (!(designRoomId.equals("default") || designRoomId.equals("null"))) {
                roomId = Long.parseLong(designRoomId);
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Design room id must be an integer string");
        }

        // check if room exists, if a long has been parsed into the roomId variable
        if (roomId != null) {
            Optional<Room> newRoomOptional = roomService.getRoomById(roomId);

            if (newRoomOptional.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Room with id " + designRoomId + " does not exist");
            }
        }

        RenovationRecord renovationRecord = renovationService.getRenovationRecordById(renovationId);

        Room room = roomService.getRoomById(roomId).orElse(null);

        RenovationDesign renovationDesign = new RenovationDesign(name, description,
                renovationRecord, room);

        renovationDesignService.saveDesignDetails(renovationDesign, designId);

        return ResponseEntity.ok().build();
    }

    /**
     * Gets a designs scene .glb file
     *
     * @param renovationId renovationId
     * @param designId     designId
     * @return ResponseEntity with resource as body
     */
    @GetMapping("renovationRecord/{renovationId}/getDesignData/{designId}")
    public ResponseEntity<FileRetrievalResponse> getDesignData(
            @PathVariable("renovationId") long renovationId,
            @PathVariable("designId") long designId
    ) {
        logger.info("Getting design data for design with id: {}", designId);
        try {
            FileRetrievalResponse response = renovationDesignService.getFileRetrievalResponse(
                    designId);
            return ResponseEntity.ok().body(response);
        } catch (IOException e) {
            logger.error("Could not retrieve design data for design with id: {}, message: {}",
                    designId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (NullPointerException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deletes the given design. Removes it from the database and removes any related data.
     *
     * @param renovationId the id of the renovation the design is on
     * @param designId     the id of the design to delete
     * @return {@code String} representation of the view to return to
     */
    @PostMapping("renovationRecord/{renovationId}/deleteDesign/{designId}")
    public String deleteDesign(@PathVariable("renovationId") Long renovationId,
            @PathVariable("designId") long designId) {
        logger.info("POST renovationRecord/{}/deleteDesign/{}", renovationId, designId);
        if (!userService.userOwnsRecord(renovationService.getRenovationRecordById(renovationId))) {
            throw new SecurityException("You do not have permission to edit this design");
        }

        try {
            renovationDesignService.deleteDesign(designId, renovationId);
        } catch (IOException e) {
            logger.error("Unable to delete design files: ", e);
        }

        return "redirect:/viewRenovation/" + renovationId;
    }

    /**
     * Get endpoint for returning ids of models that are public
     *
     * @return a list of model ids
     */
    @GetMapping("ids/model/public")
    @ResponseBody
    public List<Long> getPublicModelIds() {
        List<SceneModel> models = sceneModelService.getPublicModels();
        return models.stream().map(SceneModel::getId).toList();
    }

    /**
     * Get endpoint for returning ids of models that are specific to user i.e. uploaded themselves
     *
     * @return a list of model ids
     */
    @GetMapping("ids/model/uploaded")
    @ResponseBody
    public List<Long> getUploadedModelIds() {
        List<SceneModel> models = sceneModelService.getSceneModelsForUser();
        return models.stream().map(SceneModel::getId).toList();
    }

    /**
     * Get endpoint for getting fragment HTML representation of a scene model to put into the
     *
     * @param id    - id of model you want a fragment for
     * @param model - thymeleaf model
     * @return string formatted fragment with all model data populated inside the HTML
     */
    @GetMapping("/fragment/model/{id}")
    public String getModelFragment(@PathVariable Long id, Model model) {
        SceneModel sceneModel = sceneModelService.findById(id);

        if (sceneModel == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Model with id " + id + " not found");
        }

        // Pass through all parameters to the fragment

        model.addAttribute("name", sceneModel.getName());
        model.addAttribute("id", sceneModel.getId());
        model.addAttribute("imagePath", sceneModel.getModelImagePath());
        model.addAttribute("filePath", sceneModel.getModelPath());
        model.addAttribute("type", "model");
        model.addAttribute("modifiable", sceneModel.getUser() != null);
        model.addAttribute("emissive", sceneModel.getEmissive());
        model.addAttribute("parallelTexturePath", sceneModel.getParallelTexturePath());
        return "fragments/modelCard :: modelCard";
    }

    /**
     * POST endpoint for handling saving design thumbnails when a design is saved, either from
     * editing or creation.
     *
     * @param designId
     * @return
     */
    @PostMapping("upload/image/design/{designId}")
    public ResponseEntity<String> saveDesignImage(@PathVariable("designId") long designId,
            @RequestParam("image") MultipartFile image) {

        // validate image is not empty
        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest().body("Image cannot be empty");
        }

        // Validate user owns design
        RenovationRecord relatedRenovationRecord = renovationDesignService.getDesignById(designId)
                .getRelatedRenovationRecord();
        if (!userService.userOwnsRecord(relatedRenovationRecord)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not own this design");
        }

        // Validate image
        boolean imageValidationResult = ImageUploadValidator.validate(new HashMap<>(), image);
        if (!imageValidationResult || (image.getContentType() != null && !(image.getContentType()
                .equals("image/jpeg")))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("You must provide a valid image in JPEG format");
        }

        // Request validated - save image
        renovationDesignService.updateDesignImage(designId, image);

        return ResponseEntity.ok("Design Thumbnail Saved");
    }

    /**
     * Post end point to handle saving a new thumbnail image for a model.
     *
     * @param modelId id of model that the image is for
     * @param image   image to save
     * @return Response entity with a success or fail status
     */
    @PostMapping("upload/image/model/{modelId}")
    public ResponseEntity<String> saveModelImage(@PathVariable("modelId") long modelId,
            @RequestParam("image") MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest().body("Image cannot be empty");
        }
        // Validate image
        boolean imageValidationResult = ImageUploadValidator.validate(new HashMap<>(), image);
        if (!imageValidationResult || (image.getContentType() != null && !(image.getContentType()
                .equals("image/jpeg")))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("You must provide a valid image in JPEG format");
        }
        sceneModelService.saveCustomModelImage(modelId, image);
        return ResponseEntity.ok("Custom model image saved");
    }

    @PostMapping("uploadTexture")
    public ResponseEntity<Object> uploadTexture(@RequestParam("file") MultipartFile file)
            throws IOException {
        logger.info("POST uploadTexture");
        Map<String, String> message = new HashMap<>();
        Long textureId = sceneTextureService.uploadTexture(message, file);

        if (!message.isEmpty()) {
            return ResponseEntity.badRequest().body(message);
        }
        message.put("textureID", Long.toString(textureId));
        return ResponseEntity.ok().body(message);
    }

    @GetMapping("fragment/texture/{id}")
    public String getFragmentTexture(@PathVariable("id") long id, Model model) {
        logger.info("GET fragment texture with id: {}", id);
        SceneTexture texture = sceneTextureRepository.findById(id).orElse(null);
        if (texture != null) {
            model.addAttribute("name", texture.getName());
            model.addAttribute("filePath", texture.getTexturePath());
            model.addAttribute("imagePath", texture.getTexturePath());
            model.addAttribute("type", "texture");
            model.addAttribute("id", id);
            model.addAttribute("modifiable", texture.getUser() != null);
            model.addAttribute("emissive", false);
            model.addAttribute("parallelTexturePath", null);
        }
        return "fragments/modelCard :: modelCard";
    }

    /**
     * DELETE end point to delete a model.
     *
     * @param id the id of the model to delete.
     * @return Response with Ok, Forbidden if you do not own the model, or Bad Request otherwise.
     */
    @DeleteMapping("deleteModel/{modelId}")
    public ResponseEntity<String> deleteModel(@PathVariable("modelId") long id) {
        logger.info("DELETE delete model with id: {}", id);
        if (!sceneModelService.userOwnsModel(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not own this model.");
        }
        boolean success = sceneModelService.deleteModel(id);
        if (success) {
            return ResponseEntity.ok("Successfully deleted model with id: " + id);
        }
        return ResponseEntity.badRequest().body("Failed to delete model with id: " + id);
    }

    /**
     * DELETE end point to delete a texture.
     *
     * @param id the id of the texture to delete.
     * @return Response with Ok, Forbidden if you do not own the texture, or Bad Request otherwise.
     */
    @DeleteMapping("deleteTexture/{textureId}")
    public ResponseEntity<String> deleteTexture(@PathVariable("textureId") long id) {
        logger.info("DELETE delete texture with id: {}", id);
        if (!sceneTextureService.userOwnsTexture(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not own this texture.");
        }
        boolean success = sceneTextureService.deleteTexture(id);
        if (success) {
            return ResponseEntity.ok("Successfully deleted model with id: " + id);
        }
        return ResponseEntity.badRequest().body("Failed to delete model with id: " + id);
    }
}
