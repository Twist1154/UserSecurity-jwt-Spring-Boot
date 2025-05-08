package za.co.eyetv.usersecurity.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.co.eyetv.usersecurity.user.model.User;
import za.co.eyetv.usersecurity.user.dto.UserDTO;
import za.co.eyetv.usersecurity.auth.security.SecurityUtils;
import za.co.eyetv.usersecurity.auth.security.UserPrincipal;
import za.co.eyetv.usersecurity.user.service.UserService;

import java.util.List;
import java.util.Optional;


/**
 * REST Controller for managing {@link User} entities.
 *
 * This controller provides endpoints for:
 * - User profile retrieval for the authenticated user (/api/user/me)
 * - Administrative operations on users (/admin/users/*)
 *   - List all users
 *   - Get user by ID
 *   - Update user
 *   - Delete user
 *   - Toggle user active status
 *
 * All administrative endpoints require ADMIN role.
 *
 * @author Rethabile
 * @date 2/12/2025
 */

@RestController
@Slf4j
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get the current authenticated user's profile information
     * 
     * This endpoint returns basic profile information for the currently authenticated user.
     * It excludes sensitive information like passwords.
     * 
     * @return the current user's profile information or 401 if not authenticated
     */
    @GetMapping("/api/user/me")
    public ResponseEntity<?> getCurrentUser() {
        Optional<UserPrincipal> currentUserPrincipal = SecurityUtils.getCurrentUserPrincipal();

        if (currentUserPrincipal.isPresent()) {
            UserPrincipal userPrincipal = currentUserPrincipal.get();
            User user = userPrincipal.getUser();

            // Create a response with user details (excluding sensitive information)
            return ResponseEntity.ok(
                UserDTO.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .name(user.getName())
                    .role(user.getRole())
                    .active(user.getActive())
                    .build()
            );
        } else {
            return ResponseEntity.status(401).body("User not authenticated");
        }
    }

    /**
     * Get all users in the system
     * @return list of all users
     */
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Get a user by ID
     * @param id the user ID
     * @return the user with the specified ID
     */
    @GetMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        log.info("Fetching user with ID: {}", user);
        return ResponseEntity.ok(user);
    }

    /**
     * Update a user
     * @param id the user ID
     * @param userDetails the updated user details
     * @return the updated user
     */
    @PutMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestBody User userDetails
    ) {
        log.info("Updating user with ID: {} with details: {}", id, userDetails);
        return ResponseEntity.ok(userService.updateUser(id, userDetails));
    }

    /**
     * Delete a user
     * @param id the user ID
     * @return a message indicating the result of the operation
     */
    @DeleteMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        String result = userService.deleteUser(id);
        return ResponseEntity.ok(result);
    }

    /**
     * Toggle a user's active status
     * @param id the user ID
     * @return the updated user
     */
    @PatchMapping("/admin/users/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> toggleUserStatus(@PathVariable Long id) {
        User user = userService.getUserById(id);
        log.info("Toggling user status for user with ID: {}", user);
        User updatedUser = userService.toggleUserStatus(id);
        return ResponseEntity.ok(updatedUser);
    }
}
