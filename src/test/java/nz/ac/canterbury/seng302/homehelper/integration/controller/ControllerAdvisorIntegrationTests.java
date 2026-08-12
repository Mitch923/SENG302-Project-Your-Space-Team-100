package nz.ac.canterbury.seng302.homehelper.integration.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class ControllerAdvisorIntegrationTests {

    @Autowired
    public MockMvc mockMvc;

    @MockBean
    UserService userService;

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    public void testProfilePictureAttributeExistsWhenUserHasImage() throws Exception {
        // Arrange
        User mockUser = new User(
                "john@example.com",
                "P4$$word",
                "john",
                "doe"
        );
        mockUser.setProfileImagePath("img/user_profile_images/chicken.jpg");

        when(userService.isLoggedIn()).thenReturn(true);
        when(userService.getLoggedUser()).thenReturn(mockUser);

        // Act / Assert
        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(
                        model().attribute("profileImagePath",
                                "img/user_profile_images/chicken.jpg"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    public void testProfileImageAttributeExistsAndIsDefaultWhenUserLoggedInButHasNoImage()
            throws Exception {
        // Arrange
        User mockUser = new User(
                "john@example.com",
                "P4$$word",
                "john",
                "doe"
        );

        when(userService.isLoggedIn()).thenReturn(true);
        when(userService.getLoggedUser()).thenReturn(mockUser);

        // Act / Assert
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(
                        model().attribute("profileImagePath",
                                "/img/user_profile_images/Default_pfp.svg"));
    }
}
