package za.ac.eyetv.usersercurity.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import za.ac.eyetv.usersercurity.config.JwtProperties;
import za.ac.eyetv.usersercurity.exception.TokenValidationException;
import za.ac.eyetv.usersercurity.model.BlacklistedToken;
import za.ac.eyetv.usersercurity.model.enums.TokenType;
import za.ac.eyetv.usersercurity.repository.BlacklistedTokenRepository;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    // --- Token Utility ---

    public static String normalizeToken(String token) {
        if (token == null) return null;
        token = token.trim();
        if (token.startsWith("Bearer ")) {
            return token.substring(7).trim();
        }
        return token;
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtProperties.getExpirationMs());
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, jwtProperties.getRefreshExpirationMs());
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Updated token validation method
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()))
                    && !isTokenExpired(token)
                    && !isTokenBlacklisted(token);
        } catch (Exception e) {
            // Keep simple error log here or enhance if needed
            log.error("Token validation error during user check: {}", e.getMessage());
            return false;
        }
    }

    // New method for validating token without UserDetails
    public boolean isTokenValid(String token) {
       try {
            // Check blacklist first
            if (isTokenBlacklisted(token)) {
                log.warn("Token is blacklisted: {}", token); // Log the token
                return false;
            }
            // Check expiration *before* parsing to avoid ExpiredJwtException if possible
            if (isTokenExpired(token)) {
                log.warn("Token is expired: {}", token); // Log the token
                return false; // Explicitly return false here
            }

            // Now parse and validate signature
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);

            // If parsing succeeds and it wasn't expired or blacklisted, it's valid
            return true;

        } catch (ExpiredJwtException e) {
            // Catch specifically if isTokenExpired check somehow missed it or wasn't called first
            log.warn("JWT token is expired (caught during parsing): {}", e.getMessage());
            // Depending on desired behavior, you might throw or just return false
            // throw new TokenValidationException("JWT token is expired");
             return false;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            throw new TokenValidationException("Invalid JWT signature");
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw new TokenValidationException("Invalid JWT token");
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            throw new TokenValidationException("JWT token is unsupported");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty or argument invalid: {}", e.getMessage());
            throw new TokenValidationException("JWT claims string is empty or argument invalid");
        } catch (Exception e) { // Catch any other unexpected exceptions during validation
            log.error("Unexpected error validating token: {}", e.getMessage(), e);
            return false; // Treat unexpected errors as invalid
        }
    }


    private boolean isTokenExpired(String token) {
         try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            // If the token is expired, extractExpiration WILL throw this.
            // It's expected, so we return true.
            log.trace("Token expiration check confirmed expired via ExpiredJwtException");
            return true;
        } catch (Exception e) {
            // Any other error during extraction means we can't determine expiry, treat as problem
            log.error("Could not determine token expiration: {}", e.getMessage());
            return true; // Fail-safe: If unsure, consider it potentially unusable/expired.
        }
    }

    private Date extractExpiration(String token) {
        // This method inherently throws ExpiredJwtException if expired during parsing
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts
                    .parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("Attempted to extract claims from an expired token: {}", e.getMessage());
            // Re-throw because the caller might need to know it specifically expired
            throw e;
        } catch (Exception e) {
            // Log other parsing errors more severely
            log.error("Error parsing claims from token: {}", e.getMessage(), e); // Log stack trace for parsing errors
            // Wrap in your custom exception or rethrow appropriately
            throw new TokenValidationException("Error parsing claims from token: " + e.getMessage());
        }
    }

    public boolean isTokenBlacklisted(String token) {
        try {
            log.trace("Checking repository if token is blacklisted: {}", token);
            return blacklistedTokenRepository.existsByToken(token);
        } catch (Exception e) {
            // Log error during DB check
            log.error("Database error checking blacklisted token: {}", e.getMessage(), e); // Log stack trace
            // Fail-safe: If DB check fails, assume it *might* be blacklisted to be secure.
            return true;
        }
    }

    // --- MODIFIED METHOD ---
    // Optional: Add @Transactional if you suspect commit issues, requires import
    // @Transactional
    public void blacklistToken(String token) {
        log.debug("Attempting to blacklist token: {}", token); // Log entry
        try {
            // 1. Extract Claims (might throw exceptions if token is invalid/expired)
            Claims claims = extractAllClaims(token);
            log.debug("Claims extracted successfully for token.");

            String userEmail = claims.getSubject();
            Date expirationDate = claims.getExpiration(); // Extract as Date first

            log.debug("Token Subject (UserEmail): {}, Expiration Date: {}", userEmail, expirationDate);

            // 2. Validate extracted claims needed for saving
            if (userEmail == null || expirationDate == null) {
                 log.error("Extracted userEmail or expirationDate is null. Cannot blacklist token: {}", token);
                 // Throw specific exception or handle as needed - prevents saving bad data
                 throw new TokenValidationException("Token claims invalid for blacklisting (null subject or expiration)");
            }

            // 3. Build the entity object
            BlacklistedToken blacklistedToken = BlacklistedToken.builder()
                    .token(token)
                    .userEmail(userEmail)
                    .expiryDate(expirationDate.toInstant()) // Convert Date to Instant here
                    .blacklistedAt(Instant.now())
                    .build();

            // 4. Log the object *before* saving
            log.debug("Attempting to save BlacklistedToken to repository: {}", blacklistedToken);

            // 5. Check if it already exists (helps debug unique constraint violations)
            boolean exists = false;
            try {
                exists = blacklistedTokenRepository.existsByToken(blacklistedToken.getToken());
                log.debug("Pre-save check: Does token already exist in DB? {}", exists);
                if (exists) {
                   log.warn("Attempting to save a token that already exists in the blacklist! Token: {}", token);
                   // Optionally, you could just return here if you don't want to log an error from a duplicate save attempt
                   // return;
                }
            } catch (Exception e) {
                log.error("Error during pre-save existence check for token {}: {}", token, e.getMessage(), e);
                // Decide if you want to proceed with save attempt or throw here
            }


            // 6. Save to repository (this is the critical step)
            blacklistedTokenRepository.save(blacklistedToken);

            // 7. Log success *after* successful save
            log.info("Token blacklisted successfully and saved for user: {}", userEmail);

        } catch (ExpiredJwtException eje) {
            // Catch expired specifically if needed, maybe just log and don't blacklist?
            log.warn("Attempted to blacklist an already expired token: {}", eje.getMessage());
            // Decide if this should be an error or just ignored
            // throw new TokenValidationException("Cannot blacklist an expired token"); // Optional: Throw if needed

        } catch (DataIntegrityViolationException dive) { // Catch specific JPA/DB exceptions if possible
             log.error("Database constraint violation while saving blacklisted token. Check for duplicates or null constraints. Token: {}", token, dive);
             throw new TokenValidationException("Error saving blacklisted token due to DB constraints");

        } catch (Exception e) { // Catch-all for other exceptions
            // Log the full stack trace using the exception argument in logger
            log.error("Unexpected error during token blacklisting process. Token: {}", token, e);
            // Rethrow wrapped exception
            throw new TokenValidationException("Error blacklisting token: " + e.getMessage());
        }
    }
    // --- END OF MODIFIED METHOD ---

    // Cleanup expired tokens daily at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupExpiredTokens() {
        log.info("Starting scheduled cleanup of expired blacklisted tokens.");
        try {
            Instant now = Instant.now();
            int deletedCount = blacklistedTokenRepository.deleteExpiredTokens(now);
            log.info("Cleaned up {} expired blacklisted tokens older than {}", deletedCount, now);
        } catch (Exception e) {
            log.error("Error during scheduled cleanup of expired tokens: {}", e.getMessage(), e); // Log stack trace
        }
    }

    // Utility method to extract token from Authorization header
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
             log.warn("Attempted to extract token from invalid header: {}", authHeader);
            throw new TokenValidationException("Invalid or missing Authorization header (must start with Bearer )");
        }
        return authHeader.substring(7);
    }
}