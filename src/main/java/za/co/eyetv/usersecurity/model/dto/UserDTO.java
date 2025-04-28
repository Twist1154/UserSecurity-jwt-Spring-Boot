package za.co.eyetv.usersecurity.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.eyetv.usersecurity.model.enums.Roles;

import java.time.Instant;

/**
*@author: Rethabile Ntsekhe
*@date: @date: 09-04-2025
*/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private String name;
    private String username;
    private String password;
    private Roles role;
    private Boolean active;
    private Instant lastActive;
    private Instant createdAt;
}
