package nz.ac.canterbury.seng302.homehelper.service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import nz.ac.canterbury.seng302.homehelper.dto.FileRetrievalResponse;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationDesign;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Room;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationDesignRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.utils.FileUtilities;
import nz.ac.canterbury.seng302.homehelper.utils.UploadDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service class to handle CRUD operations without directly accessing the repository
 */
@Service
public class RenovationDesignService {

    private static Path DESIGN_THUMBNAIL_FOLDER;
    private final RenovationRecordRepository renovationRecordRepository;
    private final RenovationDesignRepository renovationDesignRepository;
    private final UserService userService;
    private final FileUtilities fileService;
    private final Logger logger = LoggerFactory.getLogger(RenovationDesignService.class);
    @Value("${static.resource.folder}")
    private String uploadFolder;

    @Autowired
    public RenovationDesignService(RenovationRecordRepository renovationRecordRepository,
            RenovationDesignRepository renovationDesignRepository, UserService userService,
            FileUtilities fileService) {

        this.renovationRecordRepository = renovationRecordRepository;
        this.renovationDesignRepository = renovationDesignRepository;
        this.userService = userService;
        this.fileService = fileService;
    }

    @PostConstruct
    public void init() {
        DESIGN_THUMBNAIL_FOLDER = Paths.get(System.getProperty("user.dir"), uploadFolder,
                "designPreviewImages");
    }

    /**
     * Gets a page of designs related to the renovation record
     *
     * @param renovation the renovation record to get designs from
     * @param page       a 1-indexed page number
     * @param pageSize   the number of objects to include on the page
     * @return a list of renovation designs
     */
    public Page<RenovationDesign> getDesignPageForRenovation(RenovationRecord renovation, int page,
            int pageSize) {
        Pageable pageRequest = PageRequest.of(page - 1,
                pageSize); // -1 because page nums are 0 indexed
        return renovationDesignRepository.findAllByRelatedRenovationRecord(renovation, pageRequest);
    }

    /**
     * Creates a new design
     *
     * @param renovationDesign design objected to be persisted
     * @return the persisted design
     */
    public RenovationDesign createDesign(RenovationDesign renovationDesign) {
        return renovationDesignRepository.save(renovationDesign);
    }

    /**
     * Saves the current state of a design overwriting the last saved state of the design
     *
     * @param renovationDesign new state of the design
     * @param designId         id of the design whose state will be altered
     */
    public void saveDesignDetails(RenovationDesign renovationDesign, long designId) {
        RenovationDesign renovationDesignToEdit = getDesignById(designId);
        renovationDesignToEdit.updateDesign(renovationDesign);
        renovationDesignRepository.save(renovationDesignToEdit);
    }

    /**
     * For a given renovation adds the design to the renovation.
     *
     * @param id The id of design to retrieve
     * @return the design found at given id
     */
    public RenovationDesign getDesignById(long id) {
        return renovationDesignRepository.getDesignById(id);
    }

    /**
     * Updates the iconName of the specified design
     *
     * @param id       Target Design
     * @param iconName Name of the selected icon
     */
    public void updateDesignIcon(Long id, String iconName) {
        if (userService.userOwnsRecord(getDesignById(id).getRelatedRenovationRecord())) {
            this.renovationDesignRepository.updateDesignIconNameById(id, iconName);
        }
    }

    /**
     * Deletes the design with the given id from the database and removes all of its related files
     * from the system. Does not raise an exception if the record or design is not found
     *
     * @param designId the id of the design to delete
     * @param recordId the id of the record to remove the design from
     */
    public void deleteDesign(Long designId, Long recordId) throws IOException {
        RenovationRecord record = this.renovationRecordRepository.findById(recordId).orElse(null);
        if (record == null) {
            logger.info("Attempted to delete design {} from renovation {} but renovation not found",
                    designId, recordId);
            return;
        }

        RenovationDesign toRemove = record.getDesignsForRenovation().stream()
                .filter(design -> design.getId().equals(designId)).findFirst().orElse(null);
        record.getDesignsForRenovation().remove(toRemove);
        if (toRemove == null) {
            logger.info(
                    "Attempted to delete design {} from renovation {} but design not found on renovation",
                    designId, recordId);
            return;
        }

        Room relatedRoom = toRemove.getRelatedRoom();
        if (relatedRoom != null && relatedRoom.getDesignsForRoom() != null) {
            toRemove.getRelatedRoom().getDesignsForRoom().remove(toRemove);
        }
        renovationRecordRepository.save(record);

        // delete thumbnail
        logger.info("Deleting competition design thumbnail with filename: {}",
                toRemove.getThumbnailFileName());
        this.fileService.deleteIfExists(UploadDirectory.DESIGN_THUMBNAILS,
                toRemove.getThumbnailFileName());
        if (toRemove.getSceneChunkDirectory() != null && !toRemove.getSceneChunkDirectory()
                .isEmpty()) {
            Path toDeletePath = UploadDirectory.SCENES.getAbsolutePath()
                    .resolve(toRemove.getSceneChunkDirectory());
            fileService.deleteDirectory(toDeletePath);
        }
    }


    public int findPageNumberOfId(long designId, int pageSize, long renovationRecordId) {
        Integer index = renovationDesignRepository.findPageNumberByIdForRelatedRenovationRecord(
                renovationRecordId, designId);
        if (index == null) {
            index = 1;
        }
        return (index - 1) / pageSize + 1;
    }

    /**
     * Saves the design image to storage and updates the database with the file path
     *
     * @param id    Design Id
     * @param image MultipartFile image (should be a jpeg)
     */
    public void updateDesignImage(Long id, MultipartFile image) {
        RenovationDesign renovationDesign = getDesignById(id);
        String currentFileName = renovationDesign.getThumbnailFileName();

        Path newImagePath;

        if (currentFileName == null || currentFileName.isEmpty()) {
            // Create new name
            String newFileName = "designPreviewImage-" + renovationDesign.getId() + ".jpeg";
            renovationDesign.setThumbnailFileName(newFileName);
            renovationDesignRepository.save(renovationDesign);
            newImagePath = DESIGN_THUMBNAIL_FOLDER.resolve(newFileName);
        } else {
            newImagePath = DESIGN_THUMBNAIL_FOLDER.resolve(currentFileName);
        }

        // Attempt to replace
        try {
            Files.deleteIfExists(newImagePath);
            Files.createDirectories(newImagePath.getParent());
            Files.write(newImagePath, image.getBytes());
        } catch (Exception e) {
            logger.error("Error attempting to save screenshot image: {}", e.getMessage(), e);
        }
    }

    /**
     * Retrieve all of a user's renovation record designs.
     *
     * @return List<RenovationDesign>
     */
    public List<RenovationDesign> getAllDesignsByUser() {
        User currentUser = userService.getLoggedUser();
        return renovationDesignRepository.findAllByUserId(currentUser.getId());
    }


    /**
     * Search the currently logged-in user's renovation designs, with optional filtering by renovation record IDs.
     *
     * @param query         the search query to filter renovation design names (case-insensitive substring match)
     * @param pageNum       the page number to retrieve (zero-based)
     * @param pageSize      the number of items per page
     * @param renovationIds a list of renovation record IDs to filter by; if empty or null, no renovation ID filter is applied
     * @return a page of renovation designs matching the query and optional renovation filter
     */
    public Page<RenovationDesign> searchUsersRenovationDesigns(
            String query,
            int pageNum,
            int pageSize,
            List<Long> renovationIds
    ) {
        User currentUser = userService.getLoggedUser();
        Pageable pageable = PageRequest.of(pageNum, pageSize);

        if (renovationIds == null || renovationIds.isEmpty()) {
            return renovationDesignRepository.findByRelatedRenovationRecordUserAndNameContainingIgnoreCase(
                    currentUser,
                    query,
                    pageable
            );
        } else {
            return renovationDesignRepository.findByRelatedRenovationRecordUserAndRelatedRenovationRecordIdInAndNameContainingIgnoreCase(
                    currentUser,
                    renovationIds,
                    query,
                    pageable
            );
        }
    }


    /**
     * Returns true if the logged-in user owns the renovation design with the given id, false
     * otherwise
     *
     * @param id of the renovation design to check
     * @return truth value of the logged-in user owning the design
     */
    public boolean userOwnsRenovationDesign(Long id) {
        User loggedInUser = userService.getLoggedUser();
        return renovationDesignRepository.getDesignById(id).getRenovationRecord().getUser().getId()
                .equals(loggedInUser.getId());
    }

    public FileRetrievalResponse getFileRetrievalResponse(Long designId) throws IOException {
        RenovationDesign design = getDesignById(designId);

        if (design.getSceneChunkDirectory() == null || design.getSceneChunkDirectory().isEmpty()
                || design.getChunkCount() <= 0) {
            throw new IOException("Missing scene chunk directory!");
        }

        FileRetrievalResponse response = new FileRetrievalResponse();
        response.setTotalChunks(design.getChunkCount());

        return response;
    }
}
