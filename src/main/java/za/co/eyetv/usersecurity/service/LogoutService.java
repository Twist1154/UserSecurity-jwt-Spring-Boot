package za.co.eyetv.usersecurity.service;

import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import za.co.eyetv.usersecurity.exception.TokenValidationException;
import za.co.eyetv.usersecurity.dto.LogoutResponse;
import za.co.eyetv.usersecurity.security.JwtService;

/**
 * @author: Rethabile Ntsekhe
 * @date: 09-04-2025
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LogoutService {
    private final JwtService jwtService;

    public LogoutResponse logout(
            @NotBlank(message = "Access token cannot be blank") String tokenToBlacklist,
            @NotBlank(message = "Refresh token cannot be blank") String refreshToken
    ) {

        // Validate tokens
        if (tokenToBlacklist == null || tokenToBlacklist.isBlank()) {
            log.warn("Invalid access token provided for logout.");
            throw new IllegalArgumentException("Access token for logout cannot be null or blank.");
        }
        if (refreshToken == null || refreshToken.isBlank()) {
            log.warn("Invalid refresh token provided for logout.");
            throw new IllegalArgumentException("Refresh token for logout cannot be null or blank.");
        }

        try {
            // Normalize tokens (strip Bearer prefix if present, trim whitespace)
            String normalizedAccessToken = JwtService.normalizeToken(tokenToBlacklist);
            String normalizedRefreshToken = JwtService.normalizeToken(refreshToken);

            jwtService.blacklistToken(normalizedAccessToken);
            jwtService.blacklistRefreshToken(normalizedRefreshToken); // Make sure you implement this!

            log.info("User logged out successfully (access and refresh tokens blacklisted)");
            return LogoutResponse.builder()
                    .message("Logout successful")
                    .build();

        } catch (TokenValidationException tve) { // Catch specific exceptions if thrown by blacklistToken
            log.error("Error during logout - token validation/blacklisting issue: {}", tve.getMessage());
            throw new RuntimeException("Logout failed: " + tve.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during logout: {}", e.getMessage(), e);
            throw new RuntimeException("Logout failed: " + e.getMessage());
        }
    }
}