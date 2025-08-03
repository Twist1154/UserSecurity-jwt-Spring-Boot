package za.co.eyetv.usersecurity.model;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom implementation of UserDetails that wraps the User entity.
 * This class provides a convenient way to access the authenticated user's information.
 */
@Getter
public class UserPrincipal implements UserDetails {

    private final User user;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this.user = user;
        this.authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }

    /**
     * Static factory method to create a UserPrincipal from a User entity
     */
    public static UserPrincipal create(User user) {
        return new UserPrincipal(user);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getActive();
    }

    /**
     * Get the user's ID
     */
    public Long getId() {
        return user.getId();
    }

    /**
     * Get the user's email
     */
    public String getEmail() {
        return user.getEmail();
    }

    /**
     * Get the user's name
     */
    public String getName() {
        return user.getName();
    }

    /**
     * Get the user's display username
     */
    public String getDisplayUsername() {
        return user.getUsername();
    }
}