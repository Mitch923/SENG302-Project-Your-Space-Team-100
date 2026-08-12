package nz.ac.canterbury.seng302.homehelper.integration.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.controller.VerificationController;
import nz.ac.canterbury.seng302.homehelper.entity.Token;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.TokenRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


@SpringBootTest
public class VerificationControllerTest {

    @Autowired
    private VerificationController verificationController;
    private MockMvc mockMvc;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenRepository tokenRepository;

    private User user;
    private Token token;


    @PostConstruct
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(verificationController).build();
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
        userRepository.deleteAll();
        tokenRepository.deleteAll();
    }

    @BeforeEach
    public void before() {
        this.user = userService.registerUser("asd@asd", passwordEncoder.encode("password"),
                "firstName",
                "lastName");
        Token newToken = new Token(user);
        userService.saveToken(newToken);
        this.token = tokenRepository.getByUserId(user.getId());
    }

    @Test
    public void testGetVerificationPage() throws Exception {
        mockMvc.perform(get("/verification?userId=" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("userId"))
                .andExpect(view().name("verificationPage"));
    }

    @Test
    public void testGivenHasParam_whenPostVerification_requestHasParam() throws Exception {
        MvcResult result = mockMvc.perform(post("/verification?userId=5")
                        .param("verificationCode", "1234"))
                .andReturn();

        HttpServletRequest request = result.getRequest();
        Assertions.assertEquals(2, request.getParameterMap().size());
        Assertions.assertEquals("1234", request.getParameter("verificationCode"));
        Assertions.assertEquals("5", request.getParameter("userId"));
    }

    @Test
    public void testGivenCodeCorrect_whenPostVerification_correctRedirect() throws Exception {
        String tokenValueFromUser = token.getToken().replace(" ", "");

        mockMvc.perform(post("/verification?userId=" + user.getId())
                        .param("verificationCode", tokenValueFromUser))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("successfulVerification",
                        "Your account has been activated, please log in"));
    }


    @Test
    public void testGivenCodeCorrect_whenPostVerification_userAuthorized() throws Exception {
        String tokenValue = token.getToken().replace(" ", "");
        Assertions.assertEquals("ROLE_UNVERIFIED", user.getAuthorities().stream()
                .findFirst().get().getAuthority());
        mockMvc.perform(post("/verification?userId=" + user.getId()).param("verificationCode",
                tokenValue));
        User updatedUser = userService.getUserById(user.getId()).get();
        Assertions.assertEquals("ROLE_USER", updatedUser.getAuthorities().stream()
                .findFirst().get().getAuthority());
    }

    @Test
    public void testGivenCodeIncorrect_whenPostVerification_correctRedirect() throws Exception {
        mockMvc.perform(post("/verification?userId=" + user.getId()).param("verificationCode",
                        "12345"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/verification?userId=" + user.getId()));
    }

    @Test
    public void testGivenCodeIncorrect_whenPostVerification_hasErrorAttribute() throws Exception {
        mockMvc.perform(post("/verification?userId=" + user.getId()).param("verificationCode",
                        "12345"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", "Signup code invalid"));
    }

}
