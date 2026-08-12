package nz.ac.canterbury.seng302.homehelper.integration.controller;

import static org.mockito.Mockito.doReturn;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import jakarta.annotation.PostConstruct;
import java.util.List;
import nz.ac.canterbury.seng302.homehelper.auth.CustomAuthenticationProvider;
import nz.ac.canterbury.seng302.homehelper.controller.ImportDesignController;
import nz.ac.canterbury.seng302.homehelper.entity.Competition;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationDesign;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.CompetitionRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationDesignRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.RenovationDesignService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.ModelMap;

@SpringBootTest
class ImportDesignControllerIntegrationTests {

    @Autowired
    ImportDesignController importDesignController;
    @Autowired
    RenovationService renovationService;
    @Autowired
    RenovationDesignService renovationDesignService;
    @Autowired
    UserService userService;
    @Autowired
    RenovationRecordRepository renovationRecordRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CustomAuthenticationProvider authenticationProvider;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @SpyBean
    private CompetitionRepository competitionRepository;

    private MockMvc mockMvc;
    private User loggedUser;
    private User otherUser;
    @Autowired
    private RenovationDesignRepository renovationDesignRepository;

    @PostConstruct
    public void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(importDesignController).build();
    }

    @BeforeEach
    void setUp() {
        doReturn(new Competition()).when(competitionRepository).getCurrentCompetition();

        String rawPassword = "P4$$word";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Create verified user
        User john = new User("john@email.com", encodedPassword, "John", "Doe");
        userService.saveUser(john);
        userService.verifyUser(john);

        // Authenticate the user
        Authentication authRequest = new UsernamePasswordAuthenticationToken(john.getEmail(),
                rawPassword);
        Authentication authResult = authenticationProvider.authenticate(authRequest);

        SecurityContextHolder.getContext().setAuthentication(authResult);
        loggedUser = john;

        // create designs for john
        RenovationRecord testRenovationRecord = new RenovationRecord(john, "testReno", "");
        testRenovationRecord = renovationService.save(testRenovationRecord);
        for (int i = 0; i < 10; i++) {
            RenovationDesign testDesign = new RenovationDesign("design" + i, "",
                    testRenovationRecord);
            renovationDesignService.createDesign(testDesign);
        }
        for (int i = 0; i < 10; i++) {
            RenovationDesign testDesign = new RenovationDesign("entry" + i, "",
                    testRenovationRecord);
            renovationDesignService.createDesign(testDesign);
        }

        //      Add another user
        User jane = new User("jane@email.com", "DumbPassword", "Jane", "Doe");
        jane.setId(500L);
        userService.saveUser(jane);
        userService.verifyUser(jane);
        otherUser = jane;
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        renovationRecordRepository.deleteAll();
    }

    @Test
    void getViewDesignsForImport_reponseOk() throws Exception {
        mockMvc.perform(get("/importDesign/").with(csrf()))
                .andExpect(status().isOk()).andExpect(view().name("competitions/importDesign"));
    }

    @Test
    void getImportDesignSearchResults_specificQuery_correctResultsAddedToModel() throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/importDesign/search/results/paged?" + "searchQuery=design1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("designs"))
                .andReturn();

        ModelMap modelMap = result.getModelAndView().getModelMap();
        List<RenovationDesign> designsInModel = (List<RenovationDesign>) modelMap.get("designs");

        Assertions.assertEquals(1, designsInModel.size());
        RenovationDesign design = designsInModel.get(0);
        Assertions.assertEquals("design1", design.getName());

    }

    @Test
    void getImportDesignSearchResults_specificQueryMultiplePages_correctResultsAddedToModel()
            throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/importDesign/search/results/paged?" + "pageNum=0&searchQuery=design").with(
                                csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("designs"))
                .andReturn();

        ModelMap modelMap = result.getModelAndView().getModelMap();
        List<RenovationDesign> designsInModel = (List<RenovationDesign>) modelMap.get("designs");

        Assertions.assertEquals(8, designsInModel.size());

        result = mockMvc.perform(
                        get("/importDesign/search/results/paged?" + "pageNum=1&searchQuery=design").with(
                                csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("designs"))
                .andReturn();

        modelMap = result.getModelAndView().getModelMap();
        designsInModel = (List<RenovationDesign>) modelMap.get("designs");

        Assertions.assertEquals(2, designsInModel.size());
    }


    @Test
    void getImportDesignSearchResults_emptyQuery_allResultsAddedToModel() throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/importDesign/search/results/paged?" + "searchQuery=").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("designs"))
                .andReturn();

        ModelMap modelMap = result.getModelAndView().getModelMap();
        List<RenovationDesign> designsInModel = (List<RenovationDesign>) modelMap.get("designs");

        Assertions.assertEquals(8, designsInModel.size());
    }


    @Test
    void getImportDesignSearchResults_emptyQueryCustomPageSize_allResultsAddedToModel()
            throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/importDesign/search/results/paged?" + "pageSize=16&searchQuery=").with(
                                csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("designs"))
                .andReturn();

        ModelMap modelMap = result.getModelAndView().getModelMap();
        List<RenovationDesign> designsInModel = (List<RenovationDesign>) modelMap.get("designs");

        Assertions.assertEquals(16, designsInModel.size());
    }



}
