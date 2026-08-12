package nz.ac.canterbury.seng302.homehelper.unit.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import nz.ac.canterbury.seng302.homehelper.controller.ControllerAdvisor;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ControllerAdvisorUnitTests {

    @Mock
    private UserService userService;

    @Mock
    private User loggedUser;

    @InjectMocks
    private ControllerAdvisor controllerAdvisor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAddUserProfile_whenUserLoggedAndHasImage() {
        // Arrange
        when(userService.isLoggedIn()).thenReturn(true);
        when(userService.getLoggedUser()).thenReturn(loggedUser);
        when(loggedUser.getProfileImagePath()).thenReturn("path/to/img");

        // Act / Assert
        assertEquals(controllerAdvisor.addUserProfile(), "path/to/img");
    }

    @Test
    public void testAddUserProfile_whenUserNotLoggedIn() {
        // Arrange
        when(userService.isLoggedIn()).thenReturn(false);

        // Act / Assert
        assertEquals(controllerAdvisor.addUserProfile(),
                "/img/user_profile_images/Default_pfp.svg");
    }

    @Test
    public void testAddUserProfile_whenUserLoggedInButDoesNotHaveImage() {
        // Arrange
        when(userService.isLoggedIn()).thenReturn(true);
        when(userService.getLoggedUser()).thenReturn(loggedUser);
        when(loggedUser.getProfileImagePath()).thenReturn(null);

        // Act / Assert
        assertEquals(controllerAdvisor.addUserProfile(),
                "/img/user_profile_images/Default_pfp.svg");
    }

}
