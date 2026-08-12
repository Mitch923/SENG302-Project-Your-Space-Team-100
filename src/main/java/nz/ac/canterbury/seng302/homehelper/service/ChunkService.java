package nz.ac.canterbury.seng302.homehelper.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import nz.ac.canterbury.seng302.homehelper.dto.ChunkUploadRequest;
import nz.ac.canterbury.seng302.homehelper.dto.ChunkUploadResponse;
import nz.ac.canterbury.seng302.homehelper.dto.InitiateUploadRequest;
import nz.ac.canterbury.seng302.homehelper.dto.InitiateUploadResponse;
import nz.ac.canterbury.seng302.homehelper.entity.CompetitionDesign;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationDesign;
import nz.ac.canterbury.seng302.homehelper.utils.FileUtilities;
import nz.ac.canterbury.seng302.homehelper.utils.TempFileTracker;
import nz.ac.canterbury.seng302.homehelper.utils.ThreadSafeTempFileDataStore;
import nz.ac.canterbury.seng302.homehelper.utils.UploadDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Provides methods to handle saving and retrieving chunks of a scene.
 */
@Service
public class ChunkService {

    private final ThreadSafeTempFileDataStore dataStore;
    private final FileUtilities fileUtilities;
    private final Logger logger = LoggerFactory.getLogger(ChunkService.class);
    private final CompetitionDesignService competitionDesignService;
    private final RenovationDesignService renovationDesignService;

    @Autowired
    public ChunkService(ThreadSafeTempFileDataStore dataStore, FileUtilities fileUtilities,
            CompetitionDesignService competitionDesignService,
            RenovationDesignService renovationDesignService) {
        this.dataStore = dataStore;
        this.fileUtilities = fileUtilities;
        this.competitionDesignService = competitionDesignService;
        this.renovationDesignService = renovationDesignService;
    }

    /**
     * Initialise a scene upload prior to receiving chunks.
     * <br>
     * Create directory as a temporary location to save chunks to until all are received. Generate a
     * random token to represent this upload and start timer to schedule deletion if upload is not
     * successful.
     *
     * @param request InitiateUploadRequest request information sent from client side.
     * @return InitiateUploadResponse response to send back to client side.
     * @throws IOException
     */
    public InitiateUploadResponse initializeChunkUpload(InitiateUploadRequest request)
            throws IOException {
        // Create temp file id and generate temp folder

        // New unique token
        String token;
        do {
            token = UUID.randomUUID().toString();
        } while (dataStore.getTokens().contains(token));

        List<String> matches = dataStore.getTokenByDesignId(request.getDesignId());
        for (String tokenMatch : matches) {
            if (tokenMatch == null || tokenMatch.isEmpty()) {
                logger.warn("Token matched null or empty");
                continue;
            }
            Path tempPath = UploadDirectory.TEMP_CHUNKS.getRelativePath().resolve(tokenMatch);
            fileUtilities.deleteDirectory(tempPath);
            dataStore.remove(tokenMatch);
        }

        int timeout = 5; // minutes
        TempFileTracker tempFileTracker = new TempFileTracker(timeout);
        tempFileTracker.setTotalChunks(request.getExpectedChunks());
        tempFileTracker.setCurrentChunks(0);
        tempFileTracker.setDesignId(request.getDesignId());
        tempFileTracker.setCompetition(request.isCompetition());

        // Create new temp folder for data
        Path tempPath = UploadDirectory.TEMP_CHUNKS.getRelativePath().resolve(token);
        fileUtilities.constructFilePathInUploads(tempPath.toString());

        dataStore.add(token, tempFileTracker);

        // Add info about temp upload to response
        InitiateUploadResponse response = new InitiateUploadResponse();
        response.setTempUploadToken(token);
        response.setTimeoutMinutes(timeout);

        return response;
    }

    /**
     * @param chunkUploadRequest
     * @param chunkData
     * @return
     * @throws IllegalAccessException
     * @throws IOException
     */
    public ChunkUploadResponse saveChunk(ChunkUploadRequest chunkUploadRequest,
            MultipartFile chunkData) throws IllegalAccessException, IOException {

        String token = chunkUploadRequest.getTempUploadToken();
        // Check id exists
        if (token == null || !dataStore.getTokens().contains(token)) {
            logger.warn("Token {} is not valid", token);
            throw new IllegalAccessException("Invalid or missing temp upload token!");
        }

        TempFileTracker tracker = dataStore.get(token);

        // Save chunk to directory
        Path tempFolder = UploadDirectory.TEMP_CHUNKS.getAbsolutePath().resolve(token);
        String fileName = "chunk-" + chunkUploadRequest.getChunkIndex();
        fileUtilities.saveBytesToFile(chunkData.getBytes(), tempFolder, fileName);
        logger.info("Saved chunk {} / {} at {}", chunkUploadRequest.getChunkIndex() + 1,
                tracker.getTotalChunks(), tempFolder);
        tracker.setCurrentChunks(tracker.getCurrentChunks() + 1);

        // Check completion
        if (tracker.isCompleted()) {
            logger.info("Chunk upload for design {} is completed", tracker.getDesignId());
            finishChunkUpload(token);
        }

        // Construct response
        ChunkUploadResponse response = new ChunkUploadResponse();
        response.setMessage("Successfully saved chunk " + chunkUploadRequest.getChunkIndex());
        response.setUploadComplete(tracker.isCompleted());
        response.setIsSuccess(true);

        return response;
    }

    /**
     * Removes a token from the datastore and completes its file saving by moving the chunks into
     * the permanent storage.
     *
     * @param token Upload Tracker Token
     * @throws IllegalAccessException If the token doesn't exist
     * @throws IOException            If there is an error saving the file.
     */
    public void finishChunkUpload(String token) throws IllegalAccessException, IOException {
        TempFileTracker tracker = dataStore.remove(token);
        if (token == null || token.isEmpty()) {
            throw new IllegalAccessException("Invalid or missing temp upload token!");
        }
        if (tracker == null) {
            throw new IllegalAccessException("Tracker with token: " + token + " does not exist");
        }

        Long designId = tracker.getDesignId();
        if (designId == null) {
            throw new NullPointerException("No design found for token: " + token);
        }

        if (tracker.isCompetition()) {
            String dirName = "competition_design_id" + designId;
            Path tempFolder = UploadDirectory.TEMP_CHUNKS.getAbsolutePath().resolve(token);
            Path targetFolder = UploadDirectory.COMPETITIONS.getAbsolutePath().resolve(dirName);

            fileUtilities.copyDirectory(tempFolder, targetFolder);

            CompetitionDesign design = competitionDesignService.getCompetitionDesignById(designId);
            design.setSceneChunkDirectory(dirName);
            design.setChunkCount(tracker.getTotalChunks());
            competitionDesignService.saveCompetitionEntry(design);
            fileUtilities.deleteDirectory(tempFolder);

        } else {
            String dirName = "design_id" + designId;
            Path tempFolder = UploadDirectory.TEMP_CHUNKS.getAbsolutePath().resolve(token);
            Path targetFolder = UploadDirectory.SCENES.getAbsolutePath().resolve(dirName);

            fileUtilities.copyDirectory(tempFolder, targetFolder);

            RenovationDesign design = renovationDesignService.getDesignById(designId);
            design.setSceneChunkDirectory("design_id" + designId);
            design.setChunkCount(tracker.getTotalChunks());
            renovationDesignService.createDesign(design);
            fileUtilities.deleteDirectory(tempFolder);
        }
    }

    public Resource getChunk(Long designId, int chunkIndex, boolean isCompetition)
            throws IOException {
        Path path;
        if (isCompetition) {
            path = UploadDirectory.COMPETITIONS.getAbsolutePath()
                    .resolve("competition_design_id" + designId).resolve("chunk-" + chunkIndex);
        } else {
            path = UploadDirectory.SCENES.getAbsolutePath().resolve("design_id" + designId)
                    .resolve("chunk-" + chunkIndex);
        }

        Resource resource = new UrlResource(path.toUri());
        if (Files.notExists(path) || !resource.exists() || !resource.isReadable()) {
            throw new IOException("File chunk " + chunkIndex + " for design " + designId
                    + " doesn't exist or is unreadable!");
        }

        return resource;
    }

    @Scheduled(fixedDelay = 60000)
    public void clearExpiredUploads() {
        dataStore.getExpiredTokens().forEach(token -> {
            if (token == null || token.isEmpty()) {
                return;
            }
            fileUtilities.deleteDirectory(
                    UploadDirectory.TEMP_CHUNKS.getAbsolutePath().resolve(token));
            dataStore.clearExpiredObjects();
        });
    }
}