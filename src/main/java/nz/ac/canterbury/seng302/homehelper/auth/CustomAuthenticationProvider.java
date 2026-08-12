package nz.ac.canterbury.seng302.homehelper.auth;

import nz.ac.canterbury.seng302.homehelper.entity.DynamicUserDetailsImpl;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Custom Authentication Provider class, to allow for handling authentication in any way we see fit.
 * * In this case using our existing {@link User}
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserService userService;
    private final PasswordEncoder encoder;
    private final UserRepository userRepository;

    @Autowired
    public CustomAuthenticationProvider(UserService userService, PasswordEncoder encoder,
            UserRepository userRepository) {
        super();
        this.userService = userService;
        this.encoder = encoder;
        this.userRepository = userRepository;
    }


    /**
     * Takes in an unauthenticated object and attempts to authenticate it based on the stored
     * name(email) and credentials(password).
     *
     * @param authentication The unauthenticated object.
     * @return A new authentication token to be stored in the security context
     */
    @Override
    public Authentication authenticate(Authentication authentication) {
        String email = String.valueOf(authentication.getName()).toLowerCase();
        String password = String.valueOf(authentication.getCredentials());
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            throw new BadCredentialsException("Invalid username or password");
        }

        // get user from username (which is email)
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        // verify password matches hash in database
        if (!encoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        // Wrap User in DynamicUserDetailsImpl
        DynamicUserDetailsImpl dynamicUserDetails = new DynamicUserDetailsImpl(user,
                userRepository);

        // Get user authorities (roles)
        boolean unverified = dynamicUserDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_UNVERIFIED"));

        if (unverified) {
            throw new DisabledException("Your account is unverified");
        }

        // password matches
        return new UsernamePasswordAuthenticationToken(dynamicUserDetails, null,
                user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}