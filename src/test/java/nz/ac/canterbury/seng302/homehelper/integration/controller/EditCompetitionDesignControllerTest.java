package nz.ac.canterbury.seng302.homehelper.integration.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import javax.imageio.ImageIO;
import nz.ac.canterbury.seng302.homehelper.controller.EditCompetitionDesignController;
import nz.ac.canterbury.seng302.homehelper.entity.CompetitionDesign;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.CompetitionDesignRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
class EditCompetitionDesignControllerTest {

    private static MockMultipartFile mockJpg;
    @Autowired
    EditCompetitionDesignController editCompetitionDesignController;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserService userService;
    @MockBean
    CompetitionDesignRepository competitionDesignRepository;
    private MockMvc mockMvc;
    @Autowired
    private HttpSession httpSession;
    @Autowired
    private AuthenticationProvider authenticationProvider;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    private static byte[] smallValidJpeg() throws IOException {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, 0xFFFFFF); // white pixel

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", baos);
        return baos.toByteArray();
    }

    @BeforeAll
    static void beforeAll() throws IOException {
        mockJpg = new MockMultipartFile(
                "image",
                "testFile.jpg",
                "image/jpeg",
                smallValidJpeg()
        );
    }

    @BeforeEach
    void before() {
        mockMvc = MockMvcBuilders.standaloneSetup(editCompetitionDesignController).build();
        testUser = new User("jane@doe.co.nz", passwordEncoder.encode("P4$$word"), "Jane",
                "Doe");
        testUser.setId(1L);
        userService.verifyUser(testUser);
        userRepository.save(testUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken("jane@doe.co.nz",
                "P4$$word");
        SecurityContextHolder.getContext()
                .setAuthentication(authenticationProvider.authenticate(authentication));
        httpSession.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
    }

    @Test
    void competitionDesignImageUploaded_FileNameIsChanged() throws Exception {
        CompetitionDesign newDesign = new CompetitionDesign(
                "Test Competition",
                "Test Description",
                null,
                null,
                testUser
        );
        newDesign.setId(1L);

        when(competitionDesignRepository.findById(1L)).thenReturn(Optional.of(newDesign));

        mockMvc
                .perform(
                        multipart(
                                "/upload/image/competitionEntry/1"
                        )
                                .file(mockJpg)
                                .with(csrf())
                )
                .andExpect(
                        status().isOk()
                );

        verify(competitionDesignRepository, times(1)).save(newDesign);
    }

    @Test
    void wrongLoggedUser_competitionDesignImageUploaded_forbidden() throws Exception {
        User wrongUser = new User(
                "email@email.com",
                "Password",
                "Mike",
                "Oxlong"
        );
        wrongUser.setId(2L);

        CompetitionDesign design = new CompetitionDesign(
                "Test Competition",
                "Test Description",
                null,
                null,
                wrongUser
        );

        design.setId(1L);

        when(competitionDesignRepository.findById(1L)).thenReturn(Optional.of(design));

        mockMvc
                .perform(
                        multipart(
                                "/upload/image/competitionEntry/1"
                        )
                                .file(mockJpg)
                                .with(csrf())
                )
                .andExpect(
                        status().isForbidden()
                );
    }

    @Test
    void userHasUnsubmimttedEntry_submitEntry_success() throws Exception {
        CompetitionDesign design = new CompetitionDesign(
                "Test Competition",
                "Test Description",
                null,
                null,
                testUser
        );
        design.setId(1L);
        when(competitionDesignRepository.findById(1L)).thenReturn(Optional.of(design));

        mockMvc.perform(post("/submitEntry/1")).andExpect(status().isOk());
        Assertions.assertTrue(design.isSubmitted());
    }

    @Test
    void anotherUserHasEntry_submitTheirEntry_fail() throws Exception {
        User wrongUser = new User(
                "email@email.com",
                "Password",
                "Mike",
                "Oxlong"
        );
        wrongUser.setId(2L);
        CompetitionDesign design = new CompetitionDesign(
                "Test Competition",
                "Test Description",
                null,
                null,
                wrongUser
        );
        design.setId(1L);
        when(competitionDesignRepository.findById(1L)).thenReturn(Optional.of(design));

        mockMvc.perform(post("/submitEntry/1")).andExpect(status().isBadRequest());
        Assertions.assertFalse(design.isSubmitted());
    }
}
