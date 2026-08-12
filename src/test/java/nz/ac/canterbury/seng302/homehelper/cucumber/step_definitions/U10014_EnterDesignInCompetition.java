package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import javax.imageio.ImageIO;
import nz.ac.canterbury.seng302.homehelper.entity.Competition;
import nz.ac.canterbury.seng302.homehelper.entity.CompetitionDesign;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationDesign;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionDesignService;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationDesignService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationService;
import nz.ac.canterbury.seng302.homehelper.utils.UploadDirectory;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Transactional
public class U10014_EnterDesignInCompetition {

    private MvcResult httpResponse;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CompetitionService competitionService;
    @Autowired
    private RenovationDesignService renovationDesignService;
    @Autowired
    private RenovationService renovationService;
    @Autowired
    private CompetitionDesignService competitionDesignService;

    private RenovationDesign renovationDesign;

    private static byte[] smallValidJpeg() throws IOException {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, 0xFFFFFF); // white pixel

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", baos);
        return baos.toByteArray();
    }


    @Given("I see the create or import modal")
    public void i_see_the_create_or_import_modal() throws Exception {
        renovationDesign = renovationDesignService.createDesign(
                new RenovationDesign("Design", "Description",
                        renovationService.getRenovationRecordById(1L), null));
        Path scenePath = UploadDirectory.SCENES.getAbsolutePath()
                .resolve("design_id" + renovationDesign.getId());
        Path imagePath = UploadDirectory.DESIGN_THUMBNAILS.getAbsolutePath()
                .resolve("designPreviewImage-" + renovationDesign.getId() + ".jpeg");
        renovationDesign.setSceneChunkDirectory("design_id" + renovationDesign.getId());
        renovationDesign.setThumbnailFileName(imagePath.toString());
        Files.createDirectories(scenePath);
        Files.write(scenePath.resolve("chunk-0"), smallValidJpeg());
        Files.write(imagePath, smallValidJpeg());

        LocalDate endDate = LocalDate.now().plusDays(1);
        Competition currentCompetition = competitionService.save(
                new Competition("Test Competition", LocalDate.of(2025, 8, 25),
                        endDate));
        httpResponse = mockMvc.perform(
                        MockMvcRequestBuilders.get("/competitionDetails/" + currentCompetition.getId())
                                .with(csrf()).param("entering", "true")).andExpect(status().isOk())
                .andReturn();
    }

    @When("I click Create")
    public void i_click_create() throws Exception {
        httpResponse = mockMvc.perform(
                        MockMvcRequestBuilders.post("/createCompetitionEntry").with(csrf()))
                .andExpect(status().is3xxRedirection()).andReturn();
    }

    @Then("A new design is created for my entry in the competition")
    public void a_new_design_is_created_for_my_entry_in_the_competition() {
        List<CompetitionDesign> designEntries = competitionService.getCurrentCompetition()
                .getEntries();
        Assertions.assertEquals(1, designEntries.size());
    }

    @Then("I am taken to the editor page for my design entry")
    public void i_am_taken_to_the_editor_page_for_my_design_entry() {
        Long designId = competitionService.getCurrentCompetition().getEntries().getFirst().getId();
        Assertions.assertEquals("/editCompetitionEntry/" + designId,
                httpResponse.getResponse().getRedirectedUrl());
    }

    @When("I click import")
    public void iClickImport() throws Exception {
        httpResponse = mockMvc.perform(
                        MockMvcRequestBuilders.get("/importDesign").with(csrf()))
                .andExpect(status().is2xxSuccessful()).andReturn();
    }

    @And("I select one of my designs and click Import")
    public void iSelectOneOfMyDesignsAndClick() throws Exception {
        httpResponse = mockMvc.perform(
                        MockMvcRequestBuilders.post("/importDesign/" + renovationDesign.getId())
                                .with(csrf()))
                .andExpect(status().is3xxRedirection()).andReturn();
    }

    @Then("That design is imported as my entry into the competition")
    public void thatDesignIsImportedAsMyEntryIntoTheCompetition() throws Exception {
        String view = httpResponse.getModelAndView().getViewName();
        long designId = Long.parseLong(view.substring(view.lastIndexOf("/") + 1));
        CompetitionDesign competitionDesign = competitionDesignService.getCompetitionDesignById(
                designId);
        Assertions.assertEquals(renovationDesign.getName(), competitionDesign.getName());
        Assertions.assertEquals(renovationDesign.getDescription(),
                competitionDesign.getDescription());
        byte[] data1 = Files.readAllBytes(UploadDirectory.SCENES.getAbsolutePath()
                .resolve("design_id" + renovationDesign.getId()).resolve("chunk-0"));
        byte[] data2 = Files.readAllBytes(UploadDirectory.COMPETITIONS.getAbsolutePath().resolve(
                UploadDirectory.COMPETITIONS.getAbsolutePath()
                        .resolve("competition_design_id" + competitionDesign.getId())
                        .resolve("chunk-0")));
        Assertions.assertArrayEquals(data1, data2);
    }
}
