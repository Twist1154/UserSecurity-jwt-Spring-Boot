package za.co.eyetv.usersecurity.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import za.co.eyetv.usersecurity.model.User;
import za.co.eyetv.usersecurity.repository.UserRepository;

import java.util.List;
/**
*@author: Rethabile Ntsekhe
*@date: @date: 09-04-2025
*/
@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);

        if (userDetails.getName() != null) {
            user.setName(userDetails.getName());
        }
        if (userDetails.getUsername() != null) {
            user.setUsername(userDetails.getUsername());
        }
        if (userDetails.getEmail() != null) {
            user.setEmail(userDetails.getEmail());
        }
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            // Encode only if the password is not already encoded
            String newPassword = userDetails.getPassword();
            if (!newPassword.equals(user.getPassword())) {
                if (!newPassword.startsWith("{bcrypt}")) { // Optional: Check if it's already encoded
                    newPassword = passwordEncoder.encode(newPassword);
                }
                user.setPassword(newPassword);
            }
        }

        if (userDetails.getActive() != null) {
            user.setActive(userDetails.getActive());
        }

        if (userDetails.getRole() != null) {
            user.setRole(userDetails.getRole());
        }

        return userRepository.save(user);
    }


    public String deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
        if(userRepository.existsById(id)) {
            throw new RuntimeException("Failed to delete user");
        }else {
            System.out.println("User deleted successfully");
            return "User deleted successfully";
        }
    }

    public User toggleUserStatus(Long id) {
        User user = getUserById(id);
        user.setActive(!user.getActive());
        return userRepository.save(user);
    }


}
