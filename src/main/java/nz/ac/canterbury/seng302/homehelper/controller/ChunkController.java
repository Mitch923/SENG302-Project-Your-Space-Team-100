package nz.ac.canterbury.seng302.homehelper.controller;

import java.io.IOException;
import java.nio.file.Files;
import nz.ac.canterbury.seng302.homehelper.dto.ChunkUploadRequest;
import nz.ac.canterbury.seng302.homehelper.dto.ChunkUploadResponse;
import nz.ac.canterbury.seng302.homehelper.dto.InitiateUploadRequest;
import nz.ac.canterbury.seng302.homehelper.dto.InitiateUploadResponse;
import nz.ac.canterbury.seng302.homehelper.entity.CompetitionDesign;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationDesign;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.ChunkService;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionDesignService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationDesignService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller to handle saving and retrieving the scene in chunks.
 */
@RestController
@RequestMapping("/chunks")
public class ChunkController {

    private final ChunkService chunkService;
    private final Logger logger = LoggerFactory.getLogger(ChunkController.class);
    private final CompetitionDesignService competitionDesignService;
    private final RenovationDesignService renovationDesignService;
    private final UserService userService;

    @Autowired
    public ChunkController(ChunkService chunkService,
            CompetitionDesignService competitionDesignService,
            RenovationDesignService renovationDesignService, UserService userService) {
        this.chunkService = chunkService;
        this.competitionDesignService = competitionDesignService;
        this.renovationDesignService = renovationDesignService;
        this.userService = userService;
    }

    /**
     * POST end point to initialise saving of a scene before sending the chunks.
     *
     * @param request InitiateUploadRequest information about the incoming chunks to be uploaded.
     * @return ResponseEntity.
     */
    @PostMapping("/initiate")
    public ResponseEntity<InitiateUploadResponse> initiateChunkUpload(
            @RequestBody InitiateUploadRequest request) {
        logger.info("Request to upload {} chunks for design id: {}", request.getExpectedChunks(),
                request.getDesignId());

        if (request.getExpectedChunks() == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            InitiateUploadResponse response = chunkService.initializeChunkUpload(request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST end point to send each chunk to after first sending a request to 'chunks/initiate'.
     *
     * @param chunkData The chunk blob.
     * @param request   ChunkUploadRequest containing extra information about the chunk eg. chunk
     *                  number/index.
     * @return ResponseEntity.
     * @throws IOException Exception while attempting to save chunk to temporary file.
     */
    @PostMapping("/upload")
    public ResponseEntity<ChunkUploadResponse> uploadChunk(
            @RequestPart MultipartFile chunkData,
            @RequestPart ChunkUploadRequest request
    ) throws IOException {
        logger.info("Received a chunk number {} / {}! Yay!", request.getChunkIndex() + 1,
                request.getTotalChunks());
        logger.info("Number of bytes in chunk: {}", chunkData.getBytes().length);
        try {
            ChunkUploadResponse response = chunkService.saveChunk(request, chunkData);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalAccessException e) {
            // Token didn't exist
            logger.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (IOException e) {
            // Error with file saving
            logger.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET end point to request individual chunks by design id number and the chunks index number.
     *
     * @param designId      ID of the design to retrieve a chunk for.
     * @param chunkIndex    the specific chunks index number.
     * @param isCompetition True if this is a competition design, False for renovation design.
     * @return ResponseEntity
     */
    @GetMapping()
    public ResponseEntity<Resource> getChunk(
            @RequestParam Long designId,
            @RequestParam int chunkIndex,
            @RequestParam boolean isCompetition
    ) {
        logger.info("GET /chunks");
        try {
            boolean isPublic;
            boolean isOwned;
            User user = userService.getLoggedUser();
            if (isCompetition) {
                CompetitionDesign competitionDesign = competitionDesignService.getCompetitionDesignById(
                        designId);
                isPublic = competitionDesign.isSubmitted();
                isOwned = user.equals(competitionDesign.getUser());
            } else {
                RenovationDesign renovationDesign = renovationDesignService.getDesignById(designId);
                isPublic = renovationDesign.getRenovationRecord().isPublicRecord();
                isOwned = user.equals(renovationDesign.getRenovationRecord().getUser());
            }
            if (!isPublic && !isOwned) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            logger.info("Getting chunk number {} for design with id: {}", chunkIndex, designId);
            Resource resource = chunkService.getChunk(designId, chunkIndex, isCompetition);
            String contentType = Files.probeContentType(resource.getFile().toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (NullPointerException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
