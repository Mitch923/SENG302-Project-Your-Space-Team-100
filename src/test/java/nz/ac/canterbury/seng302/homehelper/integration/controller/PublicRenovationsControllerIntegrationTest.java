package nz.ac.canterbury.seng302.homehelper.integration.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.SearchQuery;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
class PublicRenovationsControllerIntegrationTest {

    private final List<RenovationRecord> testRenovations = List.of(
            new RenovationRecord("Test 1", "Description"),
            new RenovationRecord("Test 2", "Description"),
            new RenovationRecord("Test 3", "Description"),
            new RenovationRecord("Test 4", "Description"),
            new RenovationRecord("Test 5", "Description")
    );
    @Autowired
    private RenovationService renovationService;
    @Autowired
    private UserService userService;
    @MockBean
    private UserRepository userRepositoryMock;
    @MockBean
    private RenovationRecordRepository renovationRecordRepositoryMock;
    @Autowired
    private AuthenticationProvider authenticationProvider;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MockMvc mockMvc;

    private static Stream<Arguments> pageCountsAndNumbers() {
        return Stream.of(
                Arguments.of(1, 1),
                Arguments.of(2, 3),
                Arguments.of(5, 3),
                Arguments.of(6, 9)
        );
    }

    @BeforeEach
    public void setup() {
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
        Page<RenovationRecord> mockPage = new PageImpl<>(testRenovations, pageable, 5);

        when(renovationRecordRepositoryMock.searchByNameOrDescription(eq(""), eq(null),
                any())).thenReturn(
                mockPage);

        testRenovations.forEach(reno -> {
            reno.setUser(new User("john@example.com", "Password1*", "John", "Doe"));
        });
    }

    @Test
    void publicRenovationsExist_GetPublicRenovations_RenovationsInModel() throws Exception {
        Map<String, Object> model = mockMvc.perform(get("/getPublicRenovations")
                        .param("count", String.valueOf(5))
                        .param("page", String.valueOf(1)))
                .andExpect(status().isOk())
                .andReturn()
                .getModelAndView()
                .getModel();

        List<RenovationRecord> publicRenovations = (List<RenovationRecord>) model.get(
                "publicRenovations");

        testRenovations.forEach(testRenovation -> {
            assertTrue(publicRenovations.contains(testRenovation));
        });
    }

    @ParameterizedTest
    @MethodSource("pageCountsAndNumbers")
    void countPage_GetPublicRenovations_CountPageAddedToModel(int count, int page)
            throws Exception {
        ResultActions result = mockMvc.perform(get("/getPublicRenovations")
                        .param("count", String.valueOf(count))
                        .param("page", String.valueOf(page)))
                .andExpect(status().isOk());
        SearchQuery searchQuery = (SearchQuery) result.andReturn().getRequest().getSession()
                .getAttribute("renovationSearchQuery");
        Assertions.assertEquals(count, searchQuery.getResultsPerPage());
        Assertions.assertEquals(page, searchQuery.getPage());
    }

    @Test
    void totalPages1_GetPublicRenovations_ModelTotalPages1() throws Exception {
        ResultActions result = mockMvc.perform(get("/getPublicRenovations")
                        .param("count", String.valueOf(5))
                        .param("page", String.valueOf(1)))
                .andExpect(status().isOk());
        SearchQuery searchQuery = (SearchQuery) result.andReturn().getRequest().getSession()
                .getAttribute("renovationSearchQuery");
        Assertions.assertEquals(1, searchQuery.getTotalPages());
    }
}
