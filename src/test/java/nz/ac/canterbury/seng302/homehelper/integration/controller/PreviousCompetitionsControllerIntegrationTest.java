package nz.ac.canterbury.seng302.homehelper.integration.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import nz.ac.canterbury.seng302.homehelper.controller.PreviousCompetitionsController;
import nz.ac.canterbury.seng302.homehelper.entity.Competition;
import nz.ac.canterbury.seng302.homehelper.entity.CompetitionDesign;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionDesignService;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.ModelMap;

@SpringBootTest
public class PreviousCompetitionsControllerIntegrationTest {

    @Autowired
    PreviousCompetitionsController controller;
    @Autowired
    CompetitionService competitionService;
    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CompetitionDesignService competitionDesignService;

    private MockMvc mockMvc;

    @BeforeEach
    public void before() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }


    @Test
    void getPreviousCompetitionsPage_competitionsAddedToModel() throws Exception {
        userService.createDefaultUsers(100);
        competitionService.createDefaultCompetitions(10);
        MvcResult result = mockMvc.perform(
                        get("/previous-competitions/paged").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("competitions"))
                .andReturn();

        ModelMap modelMap = result.getModelAndView().getModelMap();

        @SuppressWarnings("unchecked")
        List<Object[]> competitions = (List<Object[]>) modelMap.get("competitions");

        Assertions.assertEquals(4, competitions.size());

        // unpack (competition, winningDesign) tuples in model
        for (Object[] tuple : competitions) {
            Assertions.assertEquals(2, tuple.length);

            Competition competition = (Competition) tuple[0];
            CompetitionDesign winningDesign = (CompetitionDesign) tuple[1];

            Assertions.assertNotNull(competition);
            if (winningDesign != null) {
                Assertions.assertEquals(competition.getId(),
                        winningDesign.getCompetition().getId());
            }
        }
    }

}
