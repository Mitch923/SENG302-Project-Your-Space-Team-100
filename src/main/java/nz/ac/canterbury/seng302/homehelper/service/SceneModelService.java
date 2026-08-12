package nz.ac.canterbury.seng302.homehelper.service;

import static nz.ac.canterbury.seng302.homehelper.utils.UploadDirectory.CUSTOM_MODELS;
import static nz.ac.canterbury.seng302.homehelper.utils.UploadDirectory.MODEL_THUMBNAILS;
import static nz.ac.canterbury.seng302.homehelper.utils.UploadDirectory.PARALLEL_TEXTURES;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nz.ac.canterbury.seng302.homehelper.entity.SceneModel;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.SceneModelRepository;
import nz.ac.canterbury.seng302.homehelper.utils.FileUtilities;
import nz.ac.canterbury.seng302.homehelper.utils.ImageUploadValidator;
import nz.ac.canterbury.seng302.homehelper.utils.UploadDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service level class to handle managing SceneModels that represent 3D models that can be added to
 * a model.
 */
@Service
public class SceneModelService {

    private final Logger logger = LoggerFactory.getLogger(SceneModelService.class);
    private final UserService userService;
    private final SceneModelRepository sceneModelRepository;
    private final FileUtilities fileUtilities;

    @Autowired
    public SceneModelService(UserService userService, SceneModelRepository sceneModelRepository,
            FileUtilities fileUtilities) {
        this.userService = userService;
        this.sceneModelRepository = sceneModelRepository;
        this.fileUtilities = fileUtilities;
    }

    /**
     * Finds a scene model by id and returns it
     *
     * @param id - id of scene model
     * @return the scene model with matching id, or null if no scene model found
     */
    public SceneModel findById(Long id) {
        return sceneModelRepository.findById(id).orElse(null);
    }

    /**
     * Gets the models of the currently logged-in user.
     *
     * @return List of users models and all public ones
     */
    public List<SceneModel> getSceneModelsForUser() {
        User user = userService.getLoggedUser();
        List<SceneModel> models = sceneModelRepository.findSceneModelsByUser(user);
        return models;
    }

    /**
     * Gets "public" models that are provided by the system that don't have a user.
     *
     * @return List of users models and all public ones
     */
    public List<SceneModel> getPublicModels() {
        List<SceneModel> models = (sceneModelRepository.findSceneModelsByUser(null));
        return models;
    }

    /**
     * Saves the current state of a model overwriting the last saved state of the model
     *
     * @param model new state of the model
     */
    public void saveModelDetails(SceneModel model) {
        sceneModelRepository.save(model);
    }

    /**
     * Saves the provided scene data in the uploads/scenes folder
     *
     * @param modelId id of the scene update
     * @param data    .glb as a byte array
     * @throws IOException if I/O error occurs when deleting previous file
     */
    public void saveModelData(long modelId, byte[] data) throws IOException {
        SceneModel model = sceneModelRepository.getById(modelId);
        String modelFileName = "model_id" + modelId + ".glb";

        fileUtilities.saveBytesToFile(data, CUSTOM_MODELS, modelFileName);

        // This is the URL your JS should fetch
        model.setModelPath(
                CUSTOM_MODELS.getRelativePathForDB().resolve(modelFileName)
                        .toString());
        sceneModelRepository.save(model);
    }

    /**
     * Save the provided .jpeg image as new thumbnail image for model in uploads/model-images.
     *
     * @param modelId id of model to save image for
     * @param image   MultipartFile image data
     */
    public void saveCustomModelImage(Long modelId, MultipartFile image) {
        SceneModel model = sceneModelRepository.getById(modelId);
        String filename = "customModelImage" + '-' + modelId + ".jpeg";

        try {
            fileUtilities.saveMultipartFile(image, UploadDirectory.MODEL_THUMBNAILS, filename);
            model.setModelImagePath(
                    UploadDirectory.MODEL_THUMBNAILS.getRelativePathForDB().resolve(filename)
                            .toString());
            sceneModelRepository.save(model);
        } catch (IOException e) {
            logger.error("Error attempting to save model screenshot image: {}", e.getMessage(), e);
        }
    }

    /**
     * Delete a users custom model and associated data.
     *
     * @param modelId - id of the model to delete.
     * @return true if model successfully deleted.
     */
    public boolean deleteModel(Long modelId) {
        logger.info("Deleting model with id: {}", modelId);
        SceneModel sceneModel = findById(modelId);
        if (sceneModel != null) {
            logger.info("Deleting model id: {}", modelId);
            try {
                fileUtilities.deleteIfExists(CUSTOM_MODELS, sceneModel.getModelFileName());
                fileUtilities.deleteIfExists(MODEL_THUMBNAILS, sceneModel.getImageFileName());
            } catch (Exception e) {
                logger.error("Error attempting to delete files for model id {}: {}", modelId,
                        e.getMessage(), e);
                return false;
            }
        } else {
            logger.error("Unable to locate model in database with id: {}", modelId);
            return false;
        }
        sceneModelRepository.deleteById(modelId);
        return true;
    }

    /**
     * Check if the currently logged user owns the model with the given id.
     *
     * @param modelId - id of the model to check.
     * @return Truth value of user owning model
     */
    public boolean userOwnsModel(Long modelId) {
        List<SceneModel> usersModels = getSceneModelsForUser();
        return usersModels.stream().anyMatch(model -> model.getId().equals(modelId));
    }

    public SceneModel getById(Long modelId) {
        return sceneModelRepository.findById(modelId).orElse(null);
    }

    /**
     * Saves a parallel texture for an uploaded model. Validates that the file is the correct size
     * (10MB) and file type (png or jpeg)
     *
     * @param modelId id of the model to save to texture for
     * @param texture file to save
     * @return http response whether the upload was valid or not
     */
    public ResponseEntity<Object> saveParallelTexture(Long modelId, MultipartFile texture) {
        SceneModel model = getById(modelId);
        if (model == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, String> errors = new HashMap<>();
        if (!ImageUploadValidator.validateTexture(errors, texture)) {
            return ResponseEntity.badRequest().body(errors);
        }

        String fileName = PARALLEL_TEXTURES.getFileNameFromTargetFolderAndID(modelId)
                + FileUtilities.getFileExtensionFromName(texture.getOriginalFilename());
        try {
            fileUtilities.saveMultipartFile(texture, PARALLEL_TEXTURES, fileName);
        } catch (IOException e) {
            logger.warn("IOException while saving parallel texture for model id {}", modelId);
            return ResponseEntity.internalServerError().build();
        }

        model.setParallelTexturePath(
                PARALLEL_TEXTURES.getRelativePathForDB().resolve(fileName).toString());
        sceneModelRepository.save(model);
        return ResponseEntity.ok().build();
    }
}
