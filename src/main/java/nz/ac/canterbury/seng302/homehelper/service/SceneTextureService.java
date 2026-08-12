package nz.ac.canterbury.seng302.homehelper.service;


import static nz.ac.canterbury.seng302.homehelper.utils.UploadDirectory.CUSTOM_TEXTURES;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import nz.ac.canterbury.seng302.homehelper.entity.SceneTexture;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.SceneTextureRepository;
import nz.ac.canterbury.seng302.homehelper.utils.FileUtilities;
import nz.ac.canterbury.seng302.homehelper.utils.ImageUploadValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service level class to handle managing SceneTextures that represent textures for 3D models that
 * can be added to a design.
 */
@Service
public class SceneTextureService {

    private final Logger logger = LoggerFactory.getLogger(SceneTextureService.class);
    private final SceneTextureRepository sceneTextureRepository;
    private final UserService userService;
    private final FileUtilities fileService;

    @Autowired
    public SceneTextureService(SceneTextureRepository sceneTextureRepository,
            UserService userService, FileUtilities fileService) {
        this.sceneTextureRepository = sceneTextureRepository;
        this.userService = userService;
        this.fileService = fileService;
    }

    /**
     * Gets any "public" textures that are provided by the system that don't have a user.
     *
     * @return List of all public textures
     */
    public List<SceneTexture> getPublicTextures() {
        return sceneTextureRepository.findAllByUser(null);
    }

    /**
     * Gets all the custom textures of the currently logged-in user.
     *
     * @return List of all the users custom textures
     */
    public List<SceneTexture> getUsersCustomTextures() {
        User user = userService.getLoggedUser();
        return sceneTextureRepository.findAllByUser(user);
    }

    /**
     * Persists the supplied model using the save method of the SceneTextureRepository
     *
     * @param sceneTexture The model to be persisted
     */
    public void saveSceneTexture(SceneTexture sceneTexture) {
        sceneTextureRepository.save(sceneTexture);
    }


    /**
     * Saves the given MultipartFile to the filesystem with a unique filename. Creates a new
     * SceneTexture entity for this texture file, ties it to the logged-in user and saves it to the
     * Database.
     *
     * @param errors {@code Map<String, String>} Map to hold any validation errors
     * @param file   {@code MultipartFile} file to save
     * @return textures ID
     * @throws IOException if an IO Exception occurs
     */
    public Long uploadTexture(Map<String, String> errors, MultipartFile file) throws IOException {
        if (ImageUploadValidator.validateTexture(errors, file)) {
            String name = file.getOriginalFilename();
            name = name == null || name.isEmpty() ? "texture" : name.split("\\.")[0];
            User currentUser = userService.getLoggedUser();

            SceneTexture newSceneTexture = new SceneTexture();

            newSceneTexture.setUser(currentUser);
            newSceneTexture.setName(name);

            newSceneTexture = sceneTextureRepository.save(newSceneTexture);

            String newFileName =
                    "texture_id" + newSceneTexture.getId() + FileUtilities.getFileExtensionFromName(
                            file.getOriginalFilename());

            fileService.saveMultipartFile(file, CUSTOM_TEXTURES, newFileName);

            newSceneTexture.setTexturePath(
                    CUSTOM_TEXTURES.getRelativePathForDB().resolve(newFileName)
                            .toString());
            sceneTextureRepository.save(newSceneTexture);
            return newSceneTexture.getId();
        }
        return null;
    }

    public boolean userOwnsTexture(long id) {
        Optional<SceneTexture> texture = this.sceneTextureRepository.findById(id);
        if (texture.isPresent()) {
            User user = userService.getLoggedUser();
            SceneTexture sceneTexture = texture.get();
            return sceneTexture.getUser().getId().equals(user.getId());
        }
        return false;
    }

    /**
     * Delete a users custom texture and associated data.
     *
     * @param textureId - id of the texture to delete.
     * @return true if texture successfully deleted.
     */
    public boolean deleteTexture(Long textureId) {
        logger.info("Deleting texture with id: {}", textureId);
        Optional<SceneTexture> sceneTexture = this.sceneTextureRepository.findById(textureId);
        if (sceneTexture.isPresent()) {
            Path texturePath = Paths.get(sceneTexture.get().getTexturePath());
            logger.info("Deleting texture path: {}", texturePath);
            try {
                fileService.deleteIfExists(CUSTOM_TEXTURES,
                        sceneTexture.get().getTextureFileName());
            } catch (Exception e) {
                logger.error("Error attempting to delete file at location: {}. {}", texturePath,
                        e.getMessage(), e);
                return false;
            }
        } else {
            logger.error("Unable to locate model in database with id: {}", textureId);
            return false;
        }
        sceneTextureRepository.deleteById(textureId);
        return true;
    }
}
