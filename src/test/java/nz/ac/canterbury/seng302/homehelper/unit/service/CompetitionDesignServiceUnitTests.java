package nz.ac.canterbury.seng302.homehelper.unit.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import nz.ac.canterbury.seng302.homehelper.entity.CompetitionDesign;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationDesign;
import nz.ac.canterbury.seng302.homehelper.repository.CompetitionDesignRepository;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionDesignService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.utils.FileUtilities;
import nz.ac.canterbury.seng302.homehelper.utils.UploadDirectory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompetitionDesignServiceUnitTests {

    @Mock
    CompetitionDesignRepository competitionDesignRepository;
    @Mock
    UserService userService;

    @Mock
    FileUtilities fileUtilities;

    @InjectMocks
    CompetitionDesignService competitionDesignService;

    @Test
    void renovationCompetitionExists_duplicateCompetition_returnsCompetition() throws IOException {
        Mockito.when(competitionDesignRepository.save(any(CompetitionDesign.class)))
                .thenAnswer(i -> {
                    CompetitionDesign design = i.getArgument(0);
                    design.setId(1L);
                    return design;
                });
        Mockito.when(
                fileUtilities.duplicateFileForCompetition(any(UploadDirectory.class), any(
                                UploadDirectory.class), anyString(),
                        anyLong())).thenAnswer(i -> {
            UploadDirectory oldDirectory = i.getArgument(0);
            UploadDirectory newDirectory;
            if (oldDirectory.equals(UploadDirectory.SCENES)) {
                newDirectory = UploadDirectory.COMPETITIONS;
            } else {
                newDirectory = UploadDirectory.COMPETITION_THUMBNAILS;
            }
            return newDirectory.getFileNameFromTargetFolderAndID(i.getArgument(3));
        });

        doReturn(null).when(userService).getLoggedUser();

        RenovationDesign renovationDesign = new RenovationDesign("Design", "Description",
                null, null);
        renovationDesign.setSceneChunkDirectory("design_id1");
        renovationDesign.setThumbnailFileName("designPreviewImage-1");
        renovationDesign.setId(1L);
        CompetitionDesign competitionDesign = competitionDesignService.duplicateRenovationDesign(
                renovationDesign, null);

        Assertions.assertNotNull(competitionDesign);
        Assertions.assertEquals("competition_" + renovationDesign.getSceneChunkDirectory(),
                competitionDesign.getSceneChunkDirectory());
        Assertions.assertEquals("competition_thumbnail_id1",
                competitionDesign.getThumbnailFilePath());
        Assertions.assertEquals(renovationDesign.getName(), competitionDesign.getName());
        Assertions.assertEquals(renovationDesign.getDescription(),
                competitionDesign.getDescription());
    }

}
