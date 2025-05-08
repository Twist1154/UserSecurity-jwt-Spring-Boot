package za.co.eyetv.usersecurity.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
*@author: Rethabile Ntsekhe
*@date: @date: 09-04-2025
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    private String token;
    private String refreshToken;
    private String email;
    private String username;
    private String role;
}
