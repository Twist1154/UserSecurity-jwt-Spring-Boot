package za.co.eyetv.usersecurity.auth.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import za.co.eyetv.usersecurity.user.model.User;

import java.util.Optional;

/**
 * Utility class for Spring Security.
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Get the login of the current user.
     *
     * @return the login of the current user, or empty if not authenticated
     */
    public static Optional<String> getCurrentUserLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null) {
            return Optional.empty();
        }
        
        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            return Optional.of(userDetails.getUsername());
        }
        
        if (authentication.getPrincipal() instanceof String s) {
            return Optional.of(s);
        }
        
        return Optional.empty();
    }

    /**
     * Get the current user as a UserPrincipal.
     *
     * @return the current user as a UserPrincipal, or empty if not authenticated or not a UserPrincipal
     */
    public static Optional<UserPrincipal> getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null) {
            return Optional.empty();
        }
        
        if (authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return Optional.of(userPrincipal);
        }
        
        return Optional.empty();
    }

    /**
     * Get the current user.
     *
     * @return the current user, or empty if not authenticated or not a UserPrincipal
     */
    public static Optional<User> getCurrentUser() {
        return getCurrentUserPrincipal().map(UserPrincipal::getUser);
    }

    /**
     * Check if the current user has a specific authority.
     *
     * @param authority the authority to check
     * @return true if the current user has the authority, false otherwise
     */
    public static boolean hasCurrentUserAuthority(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
    }
}