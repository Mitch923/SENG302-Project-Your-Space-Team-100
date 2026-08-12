package nz.ac.canterbury.seng302.homehelper.integration.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.SearchQuery;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
class MyRenovationsSearchControllerIntegrationTest {

    private final List<RenovationRecord> testRenovations = List.of(
            new RenovationRecord("Test 1", "Description"),
            new RenovationRecord("Test 2", "Description"),
            new RenovationRecord("Test 3", "Description"),
            new RenovationRecord("Test 4", "Description"),
            new RenovationRecord("Test 5", "Description")
    );
    @Autowired
    private UserService userService;
    @MockBean
    private RenovationRecordRepository renovationRecordRepositoryMock;
    @MockBean
    private UserRepository userRepositoryMock;
    @Autowired
    private AuthenticationProvider authenticationProvider;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        // Setup Authentication
        User john = new User("john@example.com", passwordEncoder.encode("Password1*"), "John",
                "Doe");
        userService.verifyUser(john);
        when(userRepositoryMock.findByEmailIgnoreCase(any())).thenReturn(Optional.of(john));
        when(userRepositoryMock.findById(any())).thenReturn(Optional.of(john));
        when(userRepositoryMock.save(any())).thenReturn(john);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "john@example.com", "Password1*"
        );
        Authentication authenticated = authenticationProvider.authenticate(auth);
        SecurityContextHolder.getContext().setAuthentication(authenticated);

        // Set up mocks
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<RenovationRecord> mockEmptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);
        Page<RenovationRecord> mockPage = new PageImpl<>(testRenovations, pageable, 5);

        when(renovationRecordRepositoryMock.searchByNameOrDescription(eq("emptyResult"), eq(john),
                any())).thenReturn(mockEmptyPage);
        when(renovationRecordRepositoryMock.searchByNameOrDescription(eq("nonEmptyResult"),
                eq(john), any())).thenReturn(mockPage);

        testRenovations.forEach(reno -> {
            reno.setUser(new User("john@example.com", "Password1*", "John", "Doe"));
        });
    }

    @Test
    void queryWithNoResults_GetSearchMyRenovations_EmptyResult() throws Exception {
        ResultActions result = mockMvc.perform(
                        get("/searchMyRenovations").param("query", "emptyResult"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("emptyResult", true));
        SearchQuery searchQuery = (SearchQuery) result.andReturn().getRequest().getSession()
                .getAttribute("searchMyRenovationsSearchQuery");
        Assertions.assertNotNull(searchQuery);
        Assertions.assertEquals("emptyResult", searchQuery.getQuery());

    }

    @Test
    void queryWithResults_GetSearchMyRenovations_NonEmptyResult() throws Exception {
        ResultActions result = mockMvc.perform(
                        get("/searchMyRenovations").param("query", "nonEmptyResult"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("emptyResult", false));
        SearchQuery searchQuery = (SearchQuery) result.andReturn().getRequest().getSession()
                .getAttribute("searchMyRenovationsSearchQuery");
        Assertions.assertNotNull(searchQuery);
        Assertions.assertEquals("nonEmptyResult", searchQuery.getQuery());
    }

    @Test
    void queryPageCount_GetMyRenovationsSearch_AttributesAdded() throws Exception {
        int page = 1;
        int count = 3;

        MvcResult result = mockMvc.perform(
                        get("/getMyRenovationsSearch").param("query", "nonEmptyResult").param("page",
                                String.valueOf(page)).param("resultsPerPage", String.valueOf(count)))
                .andExpect(status().isOk())
                .andReturn();
        SearchQuery searchQuery = (SearchQuery) result.getRequest().getSession()
                .getAttribute("searchMyRenovationsSearchQuery");
        Assertions.assertNotNull(searchQuery);
        Assertions.assertEquals(page, searchQuery.getPage());
        Assertions.assertEquals(count, searchQuery.getResultsPerPage());
        Assertions.assertEquals("nonEmptyResult", searchQuery.getQuery());
        Assertions.assertEquals(1, searchQuery.getTotalPages());

        Map<String, Object> model = result.getModelAndView().getModel();
        List<RenovationRecord> results = (List<RenovationRecord>) model.get("searchResults");
        assertEquals(results.size(), testRenovations.size());
    }

}
