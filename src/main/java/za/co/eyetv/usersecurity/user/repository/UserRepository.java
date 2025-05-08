package za.co.eyetv.usersecurity.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.co.eyetv.usersecurity.common.model.enums.Roles;
import za.co.eyetv.usersecurity.user.model.User;

import java.time.Instant;
import java.util.Optional;

/**
 * Repository for managing {@link User} entities.
 * Provides methods for retrieving users based on their email, username, role, and other fields.
 *
 * @author Rethabile Ntsekhe
 * @date 2/3/2025
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User>  findByUsername(String username);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    Optional<User> findByUsernameOrEmail(String username, String email);

    Page<User> findByRole(Roles role, Pageable pageable);

    Page<User> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<User> findByLastActiveBefore(Instant lastActive, Pageable pageable);
}
