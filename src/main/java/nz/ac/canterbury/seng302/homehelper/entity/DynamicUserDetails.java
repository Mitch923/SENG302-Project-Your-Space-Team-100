package nz.ac.canterbury.seng302.homehelper.entity;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Extension of Spring Security's {@link UserDetails} that dynamically retrieves the latest user
 * information from the database whenever accessed. This ensures that any updates to user details
 * (such as email, roles, etc.) are immediately reflected throughout the application, including the
 * security context. By implementing this interface, user details can be fetched on demand instead
 * of being stored in memory, preventing outdated information from persisting. Author: ChatGPT
 */
public interface DynamicUserDetails extends UserDetails {

    User getFreshUser(); // Retrieves the latest user info from the database
}