package nz.ac.canterbury.seng302.homehelper.integration.controller;

import static nz.ac.canterbury.seng302.homehelper.utils.UploadDirectory.COMPETITIONS;
import static nz.ac.canterbury.seng302.homehelper.utils.UploadDirectory.COMPETITION_THUMBNAILS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import nz.ac.canterbury.seng302.homehelper.dto.DesignDataDTO;
import nz.ac.canterbury.seng302.homehelper.dto.FileRetrievalResponse;
import nz.ac.canterbury.seng302.homehelper.entity.Competition;
import nz.ac.canterbury.seng302.homehelper.entity.CompetitionDesign;
import nz.ac.canterbury.seng302.homehelper.entity.SceneModel;
import nz.ac.canterbury.seng302.homehelper.entity.SceneTexture;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.CompetitionDesignRepository;
import nz.ac.canterbury.seng302.homehelper.repository.CompetitionRepository;
import nz.ac.canterbury.seng302.homehelper.repository.SceneModelRepository;
import nz.ac.canterbury.seng302.homehelper.repository.SceneTextureRepository;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.utils.FileUtilities;
import nz.ac.canterbury.seng302.homehelper.utils.UploadDirectory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
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
class EditCompetitionDesignControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @SpyBean
    private UserService userService;
    @SpyBean
    private FileUtilities fileUtilities;
    @MockBean
    private CompetitionDesignRepository competitionDesignRepository;
    @MockBean
    private CompetitionRepository competitionRepository;
    @MockBean
    private SceneModelRepository sceneModelRepository;
    @MockBean
    private SceneTextureRepository sceneTextureRepository;


    private User user;
    private User otherUser;
    private CompetitionDesign competitionDesign;
    private Competition competition;
    private MockMultipartFile validCompetitionDesign;
    private DesignDataDTO newDesignDTO;

    private static Stream<Arguments> invalidImages() {
        return Stream.of(
                Arguments.of(new MockMultipartFile("image", "", "image/png",
                        "Invalid Image".getBytes())),
                Arguments.of(new MockMultipartFile("image", "", "image/jpeg",
                        "Invalid Image".getBytes())),
                Arguments.of(
                        new MockMultipartFile("image", "", "image/svg+xml",
                                "Invalid Image".getBytes()))
        );
    }

    // ChatGPT generated method
    private static Stream<Arguments> nonJpegImages() throws IOException {
        String svgContent = """
                <svg xmlns="http://www.w3.org/2000/svg" width="1" height="1">
                  <rect width="1" height="1" fill="black"/>
                </svg>
                """;

        BufferedImage pngImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream pngOut = new ByteArrayOutputStream();
        ImageIO.write(pngImage, "png", pngOut);

        return Stream.of(
                Arguments.of(new MockMultipartFile("image", "test.png", "image/png",
                        pngOut.toByteArray())),
                Arguments.of(new MockMultipartFile("image", "test.svg", "image/svg+xml",
                        svgContent.getBytes()))
        );
    }

    private static Stream<Arguments> invalidNamesAndDescriptions() {
        return Stream.of(
                Arguments.of("", ""),
                Arguments.of("a".repeat(256), ""),
                Arguments.of("a", "a".repeat(513))
        );
    }

    private void createTestDesignFiles(Long id) throws IOException {
        String folderName = String.format("competition_design_id%s", id);
        Path designPath = COMPETITIONS.getAbsolutePath()
                .resolve(folderName);
        Files.createDirectories(designPath);
        for (int i = 0; i < 3; i++) {
            Files.write(designPath.resolve(String.format("chunk-%s", i)), new byte[1]);
        }
        competitionDesign.setChunkCount(3);
        competitionDesign.setSceneChunkDirectory(folderName);

        this.fileUtilities.saveBytesToFile(new byte[1], COMPETITION_THUMBNAILS,
                String.format("competition_design_image_id%s.jpeg", id));
    }

    private void teardownTestDesignFiles(Long id) throws IOException {
        this.fileUtilities.deleteDirectory(COMPETITIONS.getAbsolutePath()
                .resolve(String.format("competition_design_id%s", id)));
        this.fileUtilities.deleteIfExists(COMPETITION_THUMBNAILS,
                String.format("competition_design_image_id%s.jpeg", id));
    }

    @BeforeEach
    void before() throws JsonProcessingException {

        user = new User("jane@example.com", "P4$$word", "John", "Doe");
        otherUser = new User("john@example.com", "P4$$word", "John", "Doe");
        otherUser.setId(2L);
        user.setId(1L);

        competitionDesign = new CompetitionDesign("Jane's Design", "Description", "",
                new Competition(), user);
        competitionDesign.setId(1L);

        competition = new Competition("theme", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 2));
        competition.setId(1L);
        newDesignDTO = new DesignDataDTO("New Name", "New Description", null);
        validCompetitionDesign = new MockMultipartFile("json", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(newDesignDTO));

        doReturn(user).when(userService).getLoggedUser();
        when(competitionDesignRepository.save(any(CompetitionDesign.class))).thenAnswer(i -> {
            CompetitionDesign designToSave = i.getArgument(0);
            designToSave.setId(1L);
            return designToSave;
        });

        when(competitionDesignRepository.findById(1L)).thenReturn(Optional.of(competitionDesign));

        when(competitionRepository.getCurrentCompetition())
                .thenAnswer(invocation -> competition);

    }

    @Test
    @WithMockUser(username = "jane@example.com", roles = "USER")
    void competitionEntryDataDoesntExist_getCompetitionEntryData_returnsNotFound()
            throws Exception {
        fileUtilities.deleteIfExists(COMPETITIONS, "competition_design_id1.glb");

        mockMvc.perform(get("/getCompetitionEntryData/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "jane@example.com", roles = "USER")
    void competitionEntryDataExists_getCompetitionEntryData_returnsNumberOfChunks()
            throws Exception {
        // Arrange <- Not Ai generated btw, this is the AAA testing format.
        String targetUrl = "/getCompetitionEntryData/" + competitionDesign.getId();
        createTestDesignFiles(competition.getId());

        // Act
        MvcResult mvcResult = mockMvc.perform(get(targetUrl))
                .andExpect(status().isOk()).andReturn();

        // Assert
        String responseBodyString = mvcResult.getResponse().getContentAsString();

        FileRetrievalResponse fileRetrievalResponse = objectMapper.readValue(responseBodyString,
                FileRetrievalResponse.class);
        assertEquals(competitionDesign.getChunkCount(), fileRetrievalResponse.getTotalChunks());

        teardownTestDesignFiles(competition.getId());
    }

    @Test
    @WithMockUser(username = "jane@example.com", roles = "USER")
    void nullImage_saveCompetitionEntryImage_returnsBadRequest() throws Exception {
        mockMvc.perform(multipart("/upload/image/competitionEntry/1").with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "jane@example.com", roles = "USER")
    void emptyImage_saveCompetitionEntryImage_returnsBadRequest() throws Exception {
        MockMultipartFile emptyImage = new MockMultipartFile("image", new byte[0]);

        mockMvc.perform(multipart("/upload/image/competitionEntry/1").file(emptyImage).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "jane@example.com", roles = "USER")
    void notOwnedDesign_saveCompetitionEntryImage_returnsForbidden() throws Exception {
        competitionDesign.setUser(otherUser);
        MockMultipartFile image = new MockMultipartFile("image", "", "image/jpeg",
                "Mock Image".getBytes());

        mockMvc.perform(multipart("/upload/image/competitionEntry/1").file(image).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @MethodSource("invalidImages")
    @WithMockUser(username = "jane@example.com", roles = "USER")
    void invalidImage_saveCompetitionEntryImage_returnsBadRequest(MockMultipartFile image)
            throws Exception {
        mockMvc.perform(multipart("/upload/image/competitionEntry/1").file(image).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource("nonJpegImages")
    @WithMockUser(username = "jane@example.com", roles = "USER")
    void nonJpeg_saveCompetitionEntryImage_returnsBadRequest(MockMultipartFile image)
            throws Exception {
        mockMvc.perform(multipart("/upload/image/competitionEntry/1").file(image).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "jane@example.com", roles = "USER")
    void validImage_saveCompetitionEntryImage_savesImage() throws Exception {
        // ChatGPT generated code start
        BufferedImage jpegImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream jpegOut = new ByteArrayOutputStream();
        ImageIO.write(jpegImage, "jpeg", jpegOut);
        MockMultipartFile validJpeg = new MockMultipartFile(
                "image", "test.jpeg", "image/jpeg", jpegOut.toByteArray()
        );
        // ChatGPT generated code end
        fileUtilities.deleteIfExists(COMPETITION_THUMBNAILS, "competition_thumbnail_id1.jpeg");

        mockMvc.perform(multipart("/upload/image/competitionEntry/1").file(validJpeg).with(csrf()))
                .andExpect(status().isOk());

        assertTrue(fileUtilities.fileExists(COMPETITION_THUMBNAILS,
                "competition_thumbnail_id1.jpeg"));
    }

    @ParameterizedTest
    @MethodSource("invalidNamesAndDescriptions")
    @WithMockUser(username = "jane@example.com", roles = "USER")
    void invalidNameDescription_editCompetitionEntry_returnsBadRequest(String invalidName,
            String invalidDescription) throws Exception {
        MockMultipartFile invalidDesignDTO = new MockMultipartFile("json", "",
                MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(
                new DesignDataDTO(invalidName, invalidDescription, null)));

        mockMvc.perform(multipart("/editCompetitionEntry/1")
                        .file(invalidDesignDTO)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(competitionDesignRepository, never()).save(any(CompetitionDesign.class));
        verify(fileUtilities, never()).saveMultipartFile(Mockito.any(), Mockito.any(),
                Mockito.any());
    }

    @Test
    @WithMockUser(username = "jane@example.com", roles = "USER")
    void userDoesntOwnEntry_editCompetitionEntry_returnsForbidden() throws Exception {
        competitionDesign.setUser(otherUser);

        mockMvc.perform(multipart("/editCompetitionEntry/1")
                        .file(validCompetitionDesign)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "jane@example.com", roles = "USER")
    void validRequest_editCompetitionEntry_savesNameDescription() throws Exception {
        mockMvc.perform(multipart("/editCompetitionEntry/1")
                        .file(validCompetitionDesign)
                        .with(csrf()))
                .andExpect(status().isOk());

        ArgumentCaptor<CompetitionDesign> designArgumentCaptor = ArgumentCaptor.forClass(
                CompetitionDesign.class);
        verify(competitionDesignRepository, times(1)).save(designArgumentCaptor.capture());
        CompetitionDesign savedDesign = designArgumentCaptor.getValue();
        assertEquals(newDesignDTO.getName(), savedDesign.getName());
        assertEquals(newDesignDTO.getDescription(), savedDesign.getDescription());
    }

    @Test
    @WithMockUser(username = "jane@example.com", roles = "USER")
    void userDoesntOwnDesign_getEditCompetitionEntry_returnsForbidden() throws Exception {
        competitionDesign.setUser(otherUser);

        mockMvc.perform(get("/editCompetitionEntry/1")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "jane@example.com", roles = "USER")
    void invalidDesignId_getEditCompetitionEntry_returnsNotFound() throws Exception {
        mockMvc.perform(get("/editCompetitionEntry/999")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "jane@example.com", roles = "USER")
    void validRequest_getEditCompetitionEntry_addsAttributesToModel() throws Exception {
        doReturn(List.of(new SceneModel("Public Model", null, "", ""))).when(sceneModelRepository)
                .findSceneModelsByUser(null);
        doReturn(List.of(new SceneModel("User Model", user, "", ""))).when(sceneModelRepository)
                .findSceneModelsByUser(user);
        doReturn(List.of(new SceneTexture("Public Texture", null, ""))).when(sceneTextureRepository)
                .findAllByUser(null);
        doReturn(List.of(new SceneTexture("User Texture", user, ""))).when(sceneTextureRepository)
                .findAllByUser(user);

        Map<String, Object> modelMap = Objects.requireNonNull(
                mockMvc.perform(get("/editCompetitionEntry/1"))
                        .andExpect(status().isOk()).andReturn().getModelAndView()).getModel();

        assertEquals(1L, ((CompetitionDesign) modelMap.get("design")).getId());
        assertEquals(true, modelMap.get("owned"));
        assertEquals(user.getId(), modelMap.get("ownerId"));
        assertEquals("Public Model",
                ((List<SceneModel>) modelMap.get("publicModels")).getFirst().getName());
        assertEquals("User Model",
                ((List<SceneModel>) modelMap.get("userModels")).getFirst().getName());
        assertEquals("Public Texture",
                ((List<SceneTexture>) modelMap.get("textures")).getFirst().getName());
        assertEquals("User Texture",
                ((List<SceneTexture>) modelMap.get("customTextures")).getFirst().getName());
    }

    private boolean designFilesExist(Long id) {
        return Files.isDirectory(COMPETITIONS.getAbsolutePath()
                .resolve(String.format("competition_design_id%s", id))) && fileUtilities.fileExists(
                COMPETITION_THUMBNAILS, String.format("competition_design_image_id%s.jpeg", id));
    }

    @Test
    @WithMockUser(username = "jane@example.com", roles = "USER")
    void deleteDesign_DesignOwnedByUser_RedirectToCompetition() throws Exception {
        // Stub the repository to return a design that matches current competition and owner
        when(competitionDesignRepository.findById(1L)).thenAnswer(invocation -> {
            CompetitionDesign design = new CompetitionDesign();
            design.setId(1L);
            design.setUser(user);
            design.setCompetition(competition);
            design.setSubmitted(false);
            design.setSceneChunkDirectory(String.format("competition_design_id%s", design.getId()));
            design.setThumbnailFilePath(
                    UploadDirectory.COMPETITION_THUMBNAILS.getRelativePathForDB().resolve(
                                    String.format("competition_design_image_id%s.jpeg", design.getId()))
                            .toString());
            design.getCompetition().addEntry(competitionDesign);
            return Optional.of(design);
        });

        this.createTestDesignFiles(1L);

        mockMvc.perform(post("/competitionEntry/1/delete").with(csrf()))
                .andExpect(status().isFound()); // i.e. 302 redirect
        Assertions.assertFalse(designFilesExist(1L));
        this.teardownTestDesignFiles(1L);
    }


    @Test
    @WithMockUser(username = "jane@example.com", roles = "USER")
    void deleteDesign_DesignOwnedByUser_DesignSubmitted_Forbidden() throws Exception {
        when(competitionDesignRepository.findById(anyLong()))
                .thenAnswer(invocation -> {
                    Long id = invocation.getArgument(0);
                    CompetitionDesign design = new CompetitionDesign();
                    design.setUser(user);
                    design.setId(id);
                    design.setSubmitted(true);
                    design.setCompetition(competition);
                    return Optional.of(design);
                });

        this.createTestDesignFiles(1L);
        mockMvc.perform(post("/competitionEntry/1/delete").with(csrf()))
                .andExpect(status().isForbidden());
        Assertions.assertTrue(designFilesExist(1L));
        this.teardownTestDesignFiles(1L);
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = "USER")
    void deleteDesign_DesignNotOwnedByUser_Forbidden() throws Exception {
        doReturn(otherUser).when(userService).getLoggedUser();
        when(competitionDesignRepository.findById(anyLong()))
                .thenAnswer(invocation -> {
                    Long id = invocation.getArgument(0);
                    CompetitionDesign design = new CompetitionDesign();
                    design.setUser(user);
                    design.setId(id);
                    design.setSubmitted(true);
                    design.setCompetition(competition);
                    return Optional.of(design);
                });
        this.createTestDesignFiles(1L);
        mockMvc.perform(post("/competitionEntry/1/delete").with(csrf()))
                .andExpect(status().isForbidden());
        Assertions.assertTrue(designFilesExist(1L));
        this.teardownTestDesignFiles(1L);
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = "USER")
    void deleteDesign_DesignDoesntExist_Forbidden() throws Exception {
        doReturn(otherUser).when(userService).getLoggedUser();
        // override stub to make repository return null
        when(competitionDesignRepository.findById(anyLong())).thenAnswer(
                invocation -> Optional.empty());

        this.createTestDesignFiles(1L);
        // check for forbidden
        mockMvc.perform(post("/competitionEntry/37/delete").with(csrf()))
                .andExpect(status().isForbidden());

        // check design files still exist
        Assertions.assertTrue(designFilesExist(1L));
        this.teardownTestDesignFiles(1L);
    }
}
