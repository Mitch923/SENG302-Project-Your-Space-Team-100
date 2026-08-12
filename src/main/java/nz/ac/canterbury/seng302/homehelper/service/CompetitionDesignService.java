package nz.ac.canterbury.seng302.homehelper.service;

import static nz.ac.canterbury.seng302.homehelper.utils.UploadDirectory.COMPETITIONS;
import static nz.ac.canterbury.seng302.homehelper.utils.UploadDirectory.COMPETITION_THUMBNAILS;
import static nz.ac.canterbury.seng302.homehelper.utils.UploadDirectory.DESIGN_THUMBNAILS;
import static nz.ac.canterbury.seng302.homehelper.utils.UploadDirectory.SCENES;

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Objects;
import nz.ac.canterbury.seng302.homehelper.dto.FileRetrievalResponse;
import nz.ac.canterbury.seng302.homehelper.entity.Competition;
import nz.ac.canterbury.seng302.homehelper.entity.CompetitionDesign;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationDesign;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.CompetitionDesignRepository;
import nz.ac.canterbury.seng302.homehelper.utils.FileUtilities;
import nz.ac.canterbury.seng302.homehelper.utils.ImageUploadValidator;
import nz.ac.canterbury.seng302.homehelper.utils.UploadDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CompetitionDesignService {

    private final FileUtilities fileUtilities;
    private final CompetitionDesignRepository competitionDesignRepository;
    private final UserService userService;

    Logger logger = LoggerFactory.getLogger(CompetitionDesignService.class);

    @Autowired
    public CompetitionDesignService(FileUtilities fileUtilities,
            CompetitionDesignRepository competitionDesignRepository, UserService userService) {
        this.fileUtilities = fileUtilities;
        this.competitionDesignRepository = competitionDesignRepository;
        this.userService = userService;
    }

    /**
     * Updates the competition entry with the given id to have the new name and description
     *
     * @param name        the entries new name
     * @param description the entries new description
     * @param id          of the entry to update
     */
    public void updateCompetitionEntryDetails(String name, String description, Long id) {
        CompetitionDesign entry = getCompetitionDesignById(id);
        entry.setName(name);
        entry.setDescription(description);
        saveCompetitionEntry(entry);
    }


    /**
     * Saves the given multipart file as the thumbnail image for the competition entry with the
     * given id
     *
     * @param id    of the competition entry to save the image for
     * @param image to save as the thumbnail
     * @throws IOException if an Exception occurs, reading/writing to the filesystem
     */
    public void saveCompetitionEntryImage(Long id, MultipartFile image) throws IOException {
        String fileName = COMPETITION_THUMBNAILS.getFileNameFromTargetFolderAndID(id);
        String fileExtension = Objects.requireNonNull(image.getOriginalFilename())
                .substring(image.getOriginalFilename().lastIndexOf("."));
        if (!fileExtension.isEmpty()) {
            fileName = fileName + fileExtension;
        }
        fileUtilities.saveMultipartFile(image, COMPETITION_THUMBNAILS, fileName);
        CompetitionDesign entry = getCompetitionDesignById(id);
        entry.setThumbnailFilePath(
                COMPETITION_THUMBNAILS.getRelativePathForDB().resolve(fileName).toString());
        saveCompetitionEntry(entry);
    }

    /**
     * Duplicates the given renovation design into a new competition design for the current
     * competition
     *
     * @param design      the renovation design to duplicate
     * @param competition the competition to duplicate the design into
     * @return the duplicated competition design
     * @throws IOException if an issue occurs, reading/writing to the file system
     */
    public CompetitionDesign duplicateRenovationDesign(
            RenovationDesign design, Competition competition) throws IOException {
        User loggedInUser = userService.getLoggedUser();
        CompetitionDesign competitionDesign = competitionDesignRepository.save(
                new CompetitionDesign(design.getName(), design.getDescription(), null,
                        competition, loggedInUser));
        copyDesignScene(design, competitionDesign);
        copyDesignThumbnail(design, competitionDesign);
        return saveCompetitionEntry(competitionDesign);
    }

    /**
     * Copies the designs scene data into a new file for the competition design. Updates the scene
     * file path of the competition entry
     *
     * @param design            the design to copy from
     * @param competitionDesign the design to copy into
     * @throws IOException if an issue occurs, reading/writing to the file system
     */
    private void copyDesignScene(RenovationDesign design,
            CompetitionDesign competitionDesign)
            throws IOException {

        String oldDirectory = design.getSceneChunkDirectory();
        if (oldDirectory == null || oldDirectory.isEmpty()) {
            logger.warn("Design '{}' has no scene file to copy", design.getName());
            return;
        }

        String newDirectoryName = "competition_design_id" + competitionDesign.getId();
        Path sourceDirectory = SCENES.getAbsolutePath().resolve(oldDirectory);
        Path targetDirectory = COMPETITIONS.getAbsolutePath()
                .resolve(newDirectoryName);

        fileUtilities.copyDirectory(sourceDirectory, targetDirectory);
        competitionDesign.setSceneChunkDirectory(newDirectoryName);
        competitionDesign.setChunkCount(design.getChunkCount());
        saveCompetitionEntry(competitionDesign);
    }

    /**
     * Copies the designs thumbnail into a new file for the competition design. Updates the
     * thumbnail file path of the competition entry
     *
     * @param design            the design to copy from
     * @param competitionDesign the design to copy into
     * @throws IOException if an issue occurs, reading/writing to the file system
     */
    private void copyDesignThumbnail(RenovationDesign design,
            CompetitionDesign competitionDesign)
            throws IOException {
        String oldFileName = design.getThumbnailFileName();
        if (oldFileName == null) {
            logger.warn("Design '{}' has no thumbnail file to copy", design.getName());
            return;
        }

        String newSceneThumbnail = fileUtilities.duplicateFileForCompetition(
                DESIGN_THUMBNAILS, COMPETITION_THUMBNAILS,
                oldFileName,
                competitionDesign.getId());
        competitionDesign.setThumbnailFilePath(newSceneThumbnail);
        saveCompetitionEntry(competitionDesign);
    }

    public CompetitionDesign saveCompetitionEntry(CompetitionDesign entry) {
        return competitionDesignRepository.save(entry);
    }

    /**
     * Validates the request to save a competition entry image and saves the image if valid. Returns
     * the relevant HTTP response entity
     *
     * @param id    of the competition entry to save the image of
     * @param image to save
     * @return {@code ResponseEntity<String>} relevant response based on validation
     */
    public ResponseEntity<String> validateAndSaveCompetitionEntryImage(Long id,
            MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest().body("Image cannot be empty");
        }

        if (!getCompetitionDesignById(id).getUser().equals(userService.getLoggedUser())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not own this design");
        }

        boolean imageValidationResult = ImageUploadValidator.validate(new HashMap<>(), image);
        if (!imageValidationResult || (image.getContentType() != null && !(image.getContentType()
                .equals("image/jpeg")))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("You must provide a valid image in JPEG format");
        }

        try {
            saveCompetitionEntryImage(id, image);
            return ResponseEntity.ok("Competition entry thumbnail saved");
        } catch (IOException e) {
            logger.warn("IOException while saving competition entry thumbnail", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("IOException while saving competition entry thumbnail");
        }
    }

    public CompetitionDesign getCompetitionDesignById(Long id) {
        return competitionDesignRepository.findById(id).orElse(null);
    }

    public boolean userOwnsCompetitionDesign(Long competitionDesignId) {
        CompetitionDesign competitionDesign = getCompetitionDesignById(competitionDesignId);
        if (competitionDesign == null) {
            return false;
        }
        boolean owned = Objects.equals(competitionDesign.getUser().getId(),
                userService.getLoggedUser().getId());
        logger.info("User owns competition design: {}", owned);
        return owned;
    }

    /**
     * Submit a CompetitionDesign to the current competition.
     *
     * @param id CompetitionDesign id.
     */
    public void submitCompetitionDesign(Long id) {
        CompetitionDesign design = getCompetitionDesignById(id);
        design.setSubmitted(true);
        saveCompetitionEntry(design);
    }

    /**
     * Deletes the competition entry design with the specified id. Covers deleting from the database
     * as well as removing associated files.
     *
     * @param id the id of the entry design you want to delete
     * @throws IOException if a failure occurs with deleting the design
     */
    @Transactional
    public void deleteDesign(Long id) throws IOException {
        CompetitionDesign compDesign = this.getCompetitionDesignById(id);

        // do nothing if design doesn't exist
        if (compDesign == null) {
            return;
        }

        Competition competition = compDesign.getCompetition();
        // orphanRemoval handles the delete
        competition.getEntries().remove(compDesign);
        // delete db record
        logger.info("Deleting competition design with id: {}", compDesign.getId());
        this.competitionDesignRepository.deleteById(id);
        // delete design file
        logger.info("Deleting competition design file with path: {}",
                compDesign.getThumbnailFilename());
        if (compDesign.getSceneChunkDirectory() != null && !compDesign.getSceneChunkDirectory()
                .isEmpty()) {
            this.fileUtilities.deleteDirectory(UploadDirectory.COMPETITIONS.getAbsolutePath()
                    .resolve(compDesign.getSceneChunkDirectory()));
        }

        // delete thumbnail
        logger.info("Deleting competition design thumbnail with filename: {}",
                compDesign.getThumbnailFilename());
        this.fileUtilities.deleteIfExists(UploadDirectory.COMPETITION_THUMBNAILS,
                compDesign.getThumbnailFilename());
    }

    public FileRetrievalResponse getFileRetrievalResponse(Long id) throws IOException {
        CompetitionDesign design = getCompetitionDesignById(id);

        if (design.getSceneChunkDirectory() == null || design.getSceneChunkDirectory().isEmpty()
                || design.getChunkCount() <= 0) {
            throw new IOException("Missing scene chunk directory!");
        }

        FileRetrievalResponse response = new FileRetrievalResponse();
        response.setTotalChunks(design.getChunkCount());

        return response;
    }
}
