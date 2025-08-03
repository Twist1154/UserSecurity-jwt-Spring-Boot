package za.co.eyetv.usersecurity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * DTO representing the request body for logging out.
 * Typically, the token to be invalidated is sent.
 *
 * Note: While this DTO can be used, the current implementation extracts the token
 * from the Authorization header, which is a common practice and avoids needing
 * this explicit request body if only the access token needs blacklisting.
 *
 * @author: Rethabile Ntsekhe
 * @date: 10-04-2025 // Assuming today's date based on previous comments
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class LogoutRequest {

    /**
     * The JWT (usually the access token provided in the Authorization header)
     * that the user wants to invalidate/blacklist upon logout.
     */
    @NotBlank(message = "Token cannot be blank")
    private String token;

    /*
     * Optional: If your logout process also required invalidating the refresh token,
     * you might add it here. However, your current service logic only blacklists
     * the single token passed to jwtService.blacklistToken().
     */
      @NotBlank(message = "Refresh token cannot be blank")
      private String refreshToken;

}