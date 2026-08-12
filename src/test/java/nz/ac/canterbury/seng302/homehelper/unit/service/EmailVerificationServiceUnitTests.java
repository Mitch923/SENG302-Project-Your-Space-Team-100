package nz.ac.canterbury.seng302.homehelper.unit.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.concurrent.ScheduledExecutorService;
import nz.ac.canterbury.seng302.homehelper.entity.Token;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.TokenRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.EmailVerificationService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class EmailVerificationServiceUnitTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private UserService userService;

    @Mock
    private ScheduledExecutorService scheduler;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    @Mock
    private User testUser;

    @Mock
    private Token token;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void scheduleUserDeletion_ShouldScheduleTask() {
        // Arrange
        when(testUser.getEmail()).thenReturn("test@test.com");
        when(testUser.getId()).thenReturn(1L);

        // Act
        emailVerificationService.scheduleUserDeletion(testUser);

        // Assert
        assertTrue(
                emailVerificationService.getScheduledRegisterTasks().containsKey(testUser.getId()));
    }

    @Test
    void cancelingUserDeletion_ShouldCancelTask() {
        // Arrange
        when(testUser.getEmail()).thenReturn("test@test.com");
        when(testUser.getId()).thenReturn(1L);

        // Act
        emailVerificationService.scheduleUserDeletion(testUser);
        emailVerificationService.cancelUserDeletion(testUser);

        // Assert
        assertFalse(
                emailVerificationService.getScheduledRegisterTasks().containsKey(testUser.getId()));
    }

    @Test
    void ifUserAlreadyScheduled_ShouldNotScheduleTask() {
        // Arrange
        when(testUser.getEmail()).thenReturn("test@test.com");
        when(testUser.getId()).thenReturn(1L);

        // Act
        emailVerificationService.scheduleUserDeletion(testUser);
        emailVerificationService.scheduleUserDeletion(testUser);

        // Assert
        assertEquals(emailVerificationService.getScheduledRegisterTasks().size(), 1);
    }

    @Test
    void ifUserNotScheduled_ShouldNotScheduleTask() {
        // Arrange
        when(testUser.getEmail()).thenReturn("test@test.com");
        when(testUser.getId()).thenReturn(1L);

        // Act
        emailVerificationService.cancelUserDeletion(testUser);

        // Assert
        assertTrue(emailVerificationService.getScheduledRegisterTasks().isEmpty());
    }

    @Test
    void whenGivenValidCode_OnValidation_ReturnTrue() {
        // Arrange
        when(tokenRepository.getByUserId(1L)).thenReturn(token);
        when(token.getToken()).thenReturn("1 2 3 4");

        // Act / Assert
        assertTrue(emailVerificationService.ensureValidCode("1234", 1L));
    }

    @Test
    void whenGivenInvalidCode_OnValidation_ReturnFalse() {
        // Arrange
        when(tokenRepository.getByUserId(1L)).thenReturn(token);
        when(token.getToken()).thenReturn("1 2 3 4");

        // Act / Assert
        assertFalse(emailVerificationService.ensureValidCode("5678", 1L));
    }
}
