package nz.ac.canterbury.seng302.homehelper.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;
import nz.ac.canterbury.seng302.homehelper.entity.Competition;
import nz.ac.canterbury.seng302.homehelper.entity.CompetitionDesign;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.CompetitionDesignRepository;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionDesignService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.utils.FileUtilities;
import nz.ac.canterbury.seng302.homehelper.utils.ImageUploadValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

public class CompetitionDesignServiceTest {

    @Mock
    FileUtilities fileUtilities;
    @Mock
    CompetitionDesignRepository competitionDesignRepository;
    @Mock
    UserService userService;

    private CompetitionDesignService competitionDesignService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        competitionDesignService = new CompetitionDesignService(fileUtilities,
                competitionDesignRepository, userService);
    }

    @Test
    public void nameDescription_updateCompetitionEntryDetails_setsDetails() {
        CompetitionDesign oldDesign = new CompetitionDesign("Old Name", "Old Description", "",
                new Competition(), new User("", "", "", ""));
        when(competitionDesignRepository.findById(anyLong())).thenReturn(Optional.of(oldDesign));
        String newName = "New Name";
        String newDescription = "New Description";

        competitionDesignService.updateCompetitionEntryDetails(newName, newDescription, 1L);

        ArgumentCaptor<CompetitionDesign> captor = ArgumentCaptor.forClass(CompetitionDesign.class);
        verify(competitionDesignRepository).save(captor.capture());

        CompetitionDesign competitionDesign = captor.getValue();
        assertEquals(newName, competitionDesign.getName());
        assertEquals(newDescription, competitionDesign.getDescription());
    }

    @Test
    public void emptyImage_validateAndSaveCompetitionEntryImage_returnsBadRequest() {
        MockMultipartFile file = new MockMultipartFile("image", "image.jpg", "image/jpeg",
                new byte[0]);

        ResponseEntity<String> response = competitionDesignService.validateAndSaveCompetitionEntryImage(
                1L, file);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void userDoesntOwnImage_validateAndSaveCompetitionEntryImage_returnsForbidden() {
        User user = new User("", "", "", "");
        User otherUser = new User("", "", "", "");
        user.setId(1L);
        otherUser.setId(2L);
        CompetitionDesign design = new CompetitionDesign("Old Name", "Old Description", "",
                new Competition(), otherUser);
        MockMultipartFile file = new MockMultipartFile("image", "image.jpg", "image/jpeg",
                "Mock image".getBytes());
        when(competitionDesignRepository.findById(anyLong())).thenReturn(Optional.of(design));
        when(userService.getLoggedUser()).thenReturn(user);

        ResponseEntity<String> response = competitionDesignService.validateAndSaveCompetitionEntryImage(
                1L, file);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void invalidImage_validateAndSaveCompetitionEntryImage_returnsBadRequest() {
        User user = new User("", "", "", "");
        user.setId(1L);
        CompetitionDesign design = new CompetitionDesign("Old Name", "Old Description", "",
                new Competition(), user);
        MockMultipartFile file = new MockMultipartFile("image", "image.jpg", "image/jpeg",
                "Mock image".getBytes());
        when(competitionDesignRepository.findById(anyLong())).thenReturn(Optional.of(design));
        when(userService.getLoggedUser()).thenReturn(user);

        try (MockedStatic<ImageUploadValidator> mockedValidator = Mockito.mockStatic(
                ImageUploadValidator.class)) {
            mockedValidator.when(() -> ImageUploadValidator.validate(anyMap(), eq(file)))
                    .thenReturn(false);
            ResponseEntity<String> response = competitionDesignService.validateAndSaveCompetitionEntryImage(
                    1L, file);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    @Test
    public void nonJpegImage_validateAndSaveCompetitionEntryImage_returnsBadRequest() {
        User user = new User("", "", "", "");
        user.setId(1L);
        CompetitionDesign design = new CompetitionDesign("Old Name", "Old Description", "",
                new Competition(), user);
        MockMultipartFile file = new MockMultipartFile("image", "image.png", "image/png",
                "Mock image".getBytes());
        when(competitionDesignRepository.findById(anyLong())).thenReturn(Optional.of(design));
        when(userService.getLoggedUser()).thenReturn(user);

        try (MockedStatic<ImageUploadValidator> mockedValidator = Mockito.mockStatic(
                ImageUploadValidator.class)) {
            mockedValidator.when(() -> ImageUploadValidator.validate(anyMap(), eq(file)))
                    .thenReturn(true);
            ResponseEntity<String> response = competitionDesignService.validateAndSaveCompetitionEntryImage(
                    1L, file);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    @Test
    public void validImage_validateAndSaveCompetitionEntryImage_returnsOk() throws IOException {
        User user = new User("", "", "", "");
        user.setId(1L);
        CompetitionDesign design = new CompetitionDesign("Old Name", "Old Description", "",
                new Competition(), user);
        MockMultipartFile file = new MockMultipartFile("image", "image.jpg", "image/jpeg",
                "Mock image".getBytes());
        when(competitionDesignRepository.findById(anyLong())).thenReturn(Optional.of(design));
        when(userService.getLoggedUser()).thenReturn(user);
        doNothing().when(fileUtilities).saveMultipartFile(eq(file), any(), anyString());

        try (MockedStatic<ImageUploadValidator> mockedValidator = Mockito.mockStatic(
                ImageUploadValidator.class)) {
            mockedValidator.when(() -> ImageUploadValidator.validate(anyMap(), eq(file)))
                    .thenReturn(true);
            ResponseEntity<String> response = competitionDesignService.validateAndSaveCompetitionEntryImage(
                    1L, file);

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }
}
