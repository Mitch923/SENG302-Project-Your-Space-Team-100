package nz.ac.canterbury.seng302.homehelper.unit.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Optional;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.TokenRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UserServiceTest {

    @Mock
    private static UserRepository userRepository;

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @BeforeEach
    public void beforeEach() throws NoSuchFieldException, IllegalAccessException {
        user = new User("john@example.com", "P4$$word", "John", "Doe");
        Field field = User.class.getDeclaredField("resetPasswordToken");
        field.setAccessible(true);
        field.set(user, "token");
    }

    @Test
    public void userHasToken_revokeResetPasswordToken_tokenNull() {
        when(userRepository.save(any(User.class))).thenAnswer(
                invocation -> invocation.getArgument(0));
        when(userRepository.findByResetPasswordToken("token")).thenReturn(
                Optional.ofNullable(user));

        userService.revokeResetPasswordToken("token");
        assertNull(user.getResetPasswordToken());
    }
}
