package za.ac.eyetv.usersercurity.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
*@author: Rethabile Ntsekhe
*@date: @date: 09-04-2025
*/
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    @NotBlank(message = "JWT secret cannot be empty")
    private String secret;

    @Min(value = 60000, message = "Token expiration must be at least 1 minute")
    private long expirationMs;

    @Min(value = 300000, message = "Refresh token expiration must be at least 5 minutes")
    private long refreshExpirationMs;
}
