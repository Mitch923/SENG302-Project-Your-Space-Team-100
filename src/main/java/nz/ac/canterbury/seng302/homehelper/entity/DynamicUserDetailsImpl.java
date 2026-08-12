package nz.ac.canterbury.seng302.homehelper.entity;

import java.util.Collection;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;

/**
 * Implementation of {@link DynamicUserDetails} that dynamically retrieves the latest user
 * information from the database whenever accessed. This ensures that changes to user details (such
 * as email, roles, etc.) are reflected across the entire application, including the security
 * context. By fetching the user data fresh from the repository, this class prevents stale user
 * information from being stored in memory, which is crucial for maintaining an up-to-date
 * authentication system.</p>
 * <p>
 * Author: ChatGPT
 */
public class DynamicUserDetailsImpl implements DynamicUserDetails {

    private final Long userId;
    private final UserRepository userRepository;

    public DynamicUserDetailsImpl(User user, UserRepository userRepository) {
        this.userId = user.getId();
        this.userRepository = userRepository;
    }

    private User getFreshUserInstance() {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found")); // Handle appropriately
    }

    @Override
    public User getFreshUser() {
        return getFreshUserInstance();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getFreshUserInstance().getAuthorities();
    }

    @Override
    public String getPassword() {
        return getFreshUserInstance().getPassword();
    }

    @Override
    public String getUsername() {
        return getFreshUserInstance().getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Customize as needed
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Customize as needed
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Customize as needed
    }

    @Override
    public boolean isEnabled() {
        return true; // Customize as needed
    }
}

