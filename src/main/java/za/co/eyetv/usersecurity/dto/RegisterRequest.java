package za.co.eyetv.usersecurity.dto;

import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.common.aliasing.qual.Unique;

/**
*@author: Rethabile Ntsekhe
*@date: @date: 09-04-2025
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @Unique
    private String username;
    @Email(message = "Please provide a valid email address")
    private String email;
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")
    private String password;
}
