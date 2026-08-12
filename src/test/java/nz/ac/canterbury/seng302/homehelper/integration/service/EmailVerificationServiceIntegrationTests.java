package nz.ac.canterbury.seng302.homehelper.integration.service;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.TokenRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.EmailVerificationService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class EmailVerificationServiceIntegrationTests {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TokenRepository tokenRepository;

    @MockBean
    private UserService userService;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Mock
    private User testUser;

    @BeforeEach
    void setup() {
        when(testUser.getId()).thenReturn(1L);
        when(testUser.getEmail()).thenReturn("test@test.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    }

    @AfterEach
    void teardown() {
        SecurityContextHolder.clearContext();
    }

    // Spooky Quantum Test
    @Test
    @Transactional
    void whenDeletionScheduled_ThenTaskIsAddedToTasks() {
        emailVerificationService.scheduleUserDeletion(testUser);
        assertTrue(
                emailVerificationService.getScheduledRegisterTasks().containsKey(testUser.getId()));
        // ooo spooky
    }

    @Test
    @Transactional
    void givenUserScheduledForDeletion_WhenTimeIsUp_UserIsDeleted() {
        // Arrange
        when(userService.isLoggedIn()).thenReturn(true);
        when(userService.getLoggedUser()).thenReturn(testUser);

        // Act
        emailVerificationService.scheduleUserDeletion(testUser);

        // Assert
        await().atMost(2, TimeUnit.SECONDS)
                .until(() -> emailVerificationService.getScheduledRegisterTasks().isEmpty());

        verify(userRepository, times(1)).delete(testUser);
        verify(tokenRepository, times(1)).deleteByUserId(testUser.getId());
    }

    @Test
    void givenUserScheduledForDeletion_WhenCancellationRequested_TaskIsCancelled() {
        // Arrange
        when(userService.isLoggedIn()).thenReturn(true);
        when(userService.getLoggedUser()).thenReturn(testUser);
        emailVerificationService.scheduleUserDeletion(testUser);

        // Act
        emailVerificationService.cancelUserDeletion(testUser);

        // Assert
        await().atMost(2, TimeUnit.SECONDS)
                .until(() -> emailVerificationService.getScheduledRegisterTasks().isEmpty());

        verify(userRepository, times(0)).delete(testUser);
        verify(tokenRepository, times(0)).deleteByUserId(testUser.getId());
        assertTrue(emailVerificationService.getScheduledRegisterTasks().isEmpty());
    }

    @Test
    void given10UsersScheduledForDeletion_WhenTimeIsUp_TasksAreHandledAccordingly() {
        // Arrange
        when(userService.isLoggedIn()).thenReturn(false);
        List<User> userList = new ArrayList<>();
        userList.add(testUser);

        for (long i = 2; i <= 10; i++) {
            User mockedUser = mock(User.class);
            when(mockedUser.getId()).thenReturn(i);
            when(mockedUser.getEmail()).thenReturn("test" + i + "@test.com");
            userList.add(mockedUser);
        }

        // Act
        for (User user : userList) {
            emailVerificationService.scheduleUserDeletion(user);
        }

        // Assert
        await().atMost(2, TimeUnit.SECONDS)
                .until(() -> emailVerificationService.getScheduledRegisterTasks().isEmpty());
        verify(userRepository, times(10)).delete(any(User.class));
        verify(tokenRepository, times(10)).deleteByUserId(any(Long.class));
    }
}
