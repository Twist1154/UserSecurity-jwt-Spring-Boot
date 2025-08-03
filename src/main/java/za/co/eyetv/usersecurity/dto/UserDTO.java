package za.co.eyetv.usersecurity.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import za.co.eyetv.usersecurity.model.enums.Roles;

import java.time.Instant;
import java.util.List;

/**
*@author: Rethabile Ntsekhe
*@date: @date: 09-04-2025
*/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    @Email(message = "Please provide a valid email address")
    private String email;
    private String username;
    private List<String> roles;
    private Instant lastActive = Instant.now();
}
