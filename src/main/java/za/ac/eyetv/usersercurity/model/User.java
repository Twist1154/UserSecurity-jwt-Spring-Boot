package za.ac.eyetv.usersercurity.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import za.ac.eyetv.usersercurity.model.enums.Roles;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author: Rethabile Ntsekhe
 * @date: @date: 09-04-2025
 */
@Entity
// Ensure table name is correct, usually plural 'users'
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails, Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @Email(message = "Please provide a valid email address")
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String username;

    @Column(nullable = false)
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Roles role;
    // Default value for active is true
    @Builder.Default // Add this to ensure the default value is used with Builder
    private Boolean active = true;

    @UpdateTimestamp
    private Instant lastActive;

    @CreationTimestamp
    private Instant createdAt;

    /**
     * TODO: Uncomment and implement Settings class
     */
/*    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "user-settings") // Owner of the relationship
    private Settings settings;*/
    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email; // Using email as the username for authentication
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
        return active;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User user)) return false;
        return Objects.equals(id, user.id) &&
                Objects.equals(email, user.email) &&
                Objects.equals(name, user.name) &&
                Objects.equals(username, user.username) &&
                Objects.equals(password, user.password) &&
                role == user.role &&
                Objects.equals(active, user.active) &&
                Objects.equals(lastActive, user.lastActive) &&
                Objects.equals(createdAt, user.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, name, username, password, role, active, lastActive, createdAt);
    }

    @Override
    public String toString() {
        return "User  |  \n" +
                "ID: " + id + "\n" +
                ", Email: '" + email + "\n" +
                ", Name: '" + name + "\n" +
                ", Username: '" + username + "\n" +
                ", Password: '" + password + "\n" +
                ", Role: " + role + "\n" +
                ", Active: " + active + "\n" +
                ", LastActive: " + lastActive + "\n" +
                ", CreatedAt: " + createdAt + "\n" +
                '|';
    }



}
