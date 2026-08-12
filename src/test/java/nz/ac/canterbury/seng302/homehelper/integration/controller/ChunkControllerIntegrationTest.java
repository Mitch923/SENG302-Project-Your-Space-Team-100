package nz.ac.canterbury.seng302.homehelper.integration.controller;

import static nz.ac.canterbury.seng302.homehelper.utils.UploadDirectory.DESIGN_THUMBNAILS;
import static nz.ac.canterbury.seng302.homehelper.utils.UploadDirectory.SCENES;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import nz.ac.canterbury.seng302.homehelper.dto.ChunkUploadRequest;
import nz.ac.canterbury.seng302.homehelper.dto.ChunkUploadResponse;
import nz.ac.canterbury.seng302.homehelper.dto.InitiateUploadRequest;
import nz.ac.canterbury.seng302.homehelper.dto.InitiateUploadResponse;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationDesign;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Room;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationDesignRepository;
import nz.ac.canterbury.seng302.homehelper.service.ChunkService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.utils.FileUtilities;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class ChunkControllerIntegrationTest {

    /**
     * Info: All Arrange, Act, Assert tests in this file were not made using Ai. This is how I
     * structure tests.
     * <p>
     * Read more <a href="https://semaphore.io/blog/aaa-pattern-test-automation">here</a>.
     */

    private final int chunkCount = 3;
    private final RenovationRecord renovationRecord = new RenovationRecord();
    private final Room room = new Room("Test-room", renovationRecord);
    private final RenovationDesign renovationDesign = new RenovationDesign("Test-Reno",
            "Test-Reno-Description", room);
    private String chunkFolderName;
    private InitiateUploadRequest initRequest;
    private ChunkUploadRequest uploadRequest;

    @SpyBean
    private ChunkService chunkService;

    @Autowired
    private FileUtilities fileUtilities;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private UserService userService;

    @MockBean
    private RenovationDesignRepository renovationDesignRepository;

    private void setupTestFiles() throws IOException {
        Path designPath = SCENES.getAbsolutePath()
                .resolve(chunkFolderName);
        Files.createDirectories(designPath);
        for (int i = 0; i < chunkCount; i++) {
            Files.write(designPath.resolve("chunk-" + i), new byte[1]);
        }
        renovationDesign.setChunkCount(chunkCount);
        renovationDesign.setSceneChunkDirectory(chunkFolderName);
    }

    private String getToken() throws Exception {
        String body = objectMapper.writeValueAsString(initRequest);
        MvcResult result = mockMvc.perform(
                        post("/chunks/initiate")
                                .content(body)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        String responseBody = result.getResponse().getContentAsString();
        InitiateUploadResponse response = objectMapper.readValue(responseBody,
                InitiateUploadResponse.class);
        return response.getTempUploadToken();
    }

    @BeforeEach
    void setUp() throws IOException {
        renovationDesign.setId(1L);
        chunkFolderName = "design_id" + renovationDesign.getId();
        setupTestFiles();

        initRequest = new InitiateUploadRequest();
        initRequest.setDesignId(renovationDesign.getId());
        initRequest.setExpectedChunks(chunkCount);
        initRequest.setIsCompetition(false);

        uploadRequest = new ChunkUploadRequest();

        when(renovationDesignRepository.getDesignById(renovationDesign.getId())).thenReturn(
                renovationDesign);

        User john = new User("john@example.com", "P4$$word", "John", "Doe");
        john.setId(1L);
        renovationRecord.setUser(john);
        renovationDesign.setRenovationRecord(renovationRecord);

        doReturn(john).when(userService).getLoggedUser();
    }

    @AfterEach
    void tearDown() throws IOException {
        this.fileUtilities.deleteDirectory(SCENES.getAbsolutePath()
                .resolve(String.format("competition_design_id%s", renovationDesign.getId())));
        this.fileUtilities.deleteIfExists(DESIGN_THUMBNAILS,
                String.format("competition_design_image_id%s.jpeg", renovationDesign.getId()));
    }

    @Test
    @WithMockUser
    void validInitRequest_initiateChunkUpload_returnsValidResponse() throws Exception {
        // Arrange
        String targetUrl = "/chunks/initiate";
        String body = objectMapper.writeValueAsString(initRequest);

        // Act
        MvcResult result = mockMvc.perform(
                        post(targetUrl)
                                .content(body)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        String responseBody = result.getResponse().getContentAsString();
        InitiateUploadResponse response = objectMapper.readValue(responseBody,
                InitiateUploadResponse.class);

        assertNotNull(response.getTempUploadToken());
        verify(chunkService, times(1)).initializeChunkUpload(any());
    }

    @Test
    @WithMockUser
    void invalidInitRequest_initiateChunkUpload_returns400() throws Exception {
        // Arrange
        String targetUrl = "/chunks/initiate";
        initRequest.setExpectedChunks(null);
        String body = objectMapper.writeValueAsString(initRequest);

        // Act
        mockMvc.perform(
                        post(targetUrl)
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                // Assert
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void validChunkUploadRequest_uploadChunk_returnsValidResponse() throws Exception {
        // Arrange
        String targetUrl = "/chunks/upload";
        String token = getToken();
        uploadRequest.setTempUploadToken(token);
        uploadRequest.setChunkIndex(0);
        MockMultipartFile chunkData = new MockMultipartFile("chunkData", new byte[1]);

        String requestJson = objectMapper.writeValueAsString(uploadRequest);

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                "application/json",
                requestJson.getBytes());

        // Act
        MvcResult result = mockMvc.perform(
                        multipart(targetUrl)
                                .file(chunkData)
                                .file(requestPart)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .with(csrf())
                )
                .andExpect(status().isCreated())
                .andReturn();

        // Assert
        String responseBody = result.getResponse().getContentAsString();
        ChunkUploadResponse response = objectMapper.readValue(responseBody,
                ChunkUploadResponse.class);
        assertTrue(response.isSuccess());
        assertFalse(response.isUploadComplete());
    }

    @Test
    @WithMockUser
    void afterFinalChunkUpload_uploadChunk_returnsForbidden() throws Exception {
        // Arrange
        String targetUrl = "/chunks/upload";
        String token = getToken();
        uploadRequest.setTempUploadToken(token);

        // Make three other requests to match chunk sizes
        for (int i = 0; i < 3; i++) {
            uploadRequest.setChunkIndex(i);

            MockMultipartFile chunkData = new MockMultipartFile("chunkData", new byte[1]);

            String requestJson = objectMapper.writeValueAsString(uploadRequest);

            MockMultipartFile requestPart = new MockMultipartFile(
                    "request",
                    "",
                    "application/json",
                    requestJson.getBytes());

            mockMvc.perform(
                            multipart(targetUrl)
                                    .file(chunkData)
                                    .file(requestPart)
                                    .contentType(MediaType.MULTIPART_FORM_DATA)
                                    .with(csrf())
                    )
                    .andExpect(status().isCreated())
                    .andReturn();
        }

        MockMultipartFile chunkData = new MockMultipartFile("chunkData", new byte[1]);

        String requestJson = objectMapper.writeValueAsString(uploadRequest);

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                "application/json",
                requestJson.getBytes());

        mockMvc.perform(
                        multipart(targetUrl)
                                .file(chunkData)
                                .file(requestPart)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .with(csrf())
                )
                // Assert
                .andExpect(status().isForbidden())
                .andReturn();

    }

    @Test
    @WithMockUser
    void validRequest_getChunk_returnsValidResponse() throws Exception {
        // Arrange
        String targetUrl = "/chunks";
        int chunkIndex = 0;
        boolean isCompetition = false;

        // Act
        mockMvc.perform(
                        get(targetUrl)
                                .param("chunkIndex", String.valueOf(chunkIndex))
                                .param("isCompetition", String.valueOf(isCompetition))
                                .param("designId", String.valueOf(renovationDesign.getId()))
                )
                // Assert
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes(new byte[1]));
    }

    @Test
    @WithMockUser
    void missingId_getChunk_returns404() throws Exception {
        // Arrange
        String targetUrl = "/chunks";
        int chunkIndex = 0;
        boolean isCompetition = false;
        Long wrongId = 14L;

        // Act
        mockMvc.perform(
                        get(targetUrl)
                                .param("chunkIndex", String.valueOf(chunkIndex))
                                .param("isCompetition", String.valueOf(isCompetition))
                                .param("designId", String.valueOf(wrongId))
                )
                // Assert
                .andExpect(status().isNotFound());
    }
}
