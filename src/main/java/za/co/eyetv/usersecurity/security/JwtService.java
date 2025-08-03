package za.co.eyetv.usersecurity.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import za.co.eyetv.usersecurity.config.JwtProperties;
import za.co.eyetv.usersecurity.exception.TokenValidationException;
import za.co.eyetv.usersecurity.model.BlacklistedToken;
import za.co.eyetv.usersecurity.model.enums.TokenType;
import za.co.eyetv.usersecurity.repository.BlacklistedTokenRepository;

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
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUserRole(String token) {
        // Assuming the role is stored as a claim, e.g. { ..., "role": "ADMIN", ... }
        return extractClaim(token, claims -> claims.get("role", String.class));
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

    // --- Validation ---

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()))
                    && !isTokenExpired(token)
                    && !isTokenBlacklisted(token, TokenType.ACCESS);
        } catch (Exception e) {
            log.error("Token validation error during user check: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            // Prefer the access check here -- adjust as needed
            if (isTokenBlacklisted(token, TokenType.ACCESS)) {
                log.warn("Access token is blacklisted: {}", token);
                return false;
            }
            if (isTokenExpired(token)) {
                log.warn("Token is expired: {}", token);
                return false;
            }
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired (caught during parsing): {}", e.getMessage());
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
        } catch (Exception e) {
            log.error("Unexpected error validating token: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            log.trace("Token expiration check confirmed expired via ExpiredJwtException");
            return true;
        } catch (Exception e) {
            log.error("Could not determine token expiration: {}", e.getMessage());
            return true;
        }
    }

    private Date extractExpiration(String token) {
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
            throw e;
        } catch (Exception e) {
            log.error("Error parsing claims from token: {}", e.getMessage(), e);
            throw new TokenValidationException("Error parsing claims from token: " + e.getMessage());
        }
    }

    // --- Blacklist Check ---

    public boolean isTokenBlacklisted(String token, TokenType type) {
        try {
            token = normalizeToken(token);
            log.trace("Checking repository if token is blacklisted ({}): {}", type, token);
            return blacklistedTokenRepository.existsByTokenAndTokenType(token, type);
        } catch (Exception e) {
            log.error("Database error checking blacklisted token: {}", e.getMessage(), e);
            // Fail-safe
            return true;
        }
    }

    // --- Blacklist Token Methods ---

    public void blacklistToken(String token) {
        blacklistTokenInternal(token, TokenType.ACCESS);
    }

    public void blacklistRefreshToken(String token) {
        blacklistTokenInternal(token, TokenType.REFRESH);
    }

    private void blacklistTokenInternal(String token, TokenType type) {
        token = normalizeToken(token);
        log.debug("Attempting to blacklist {} token: {}", type, token);
        try {
            Claims claims = extractAllClaims(token);
            String userEmail = claims.getSubject();
            Date expirationDate = claims.getExpiration();

            if (userEmail == null || expirationDate == null) {
                log.error("Extracted userEmail or expirationDate is null. Cannot blacklist token: {}", token);
                throw new TokenValidationException("Token claims invalid for blacklisting (null subject or expiration)");
            }

            boolean exists = blacklistedTokenRepository.existsByTokenAndTokenType(token, type);
            if (exists) {
                log.warn("{} Token already blacklisted! Token: {}", type, token);
                return;
            }

            BlacklistedToken blacklistedToken = BlacklistedToken.builder()
                    .token(token)
                    .userEmail(userEmail)
                    .expiryDate(expirationDate.toInstant())
                    .blacklistedAt(Instant.now())
                    .tokenType(type)
                    .build();

            blacklistedTokenRepository.save(blacklistedToken);
            log.info("{} token blacklisted successfully and saved for user: {}", type, userEmail);

        } catch (ExpiredJwtException eje) {
            log.warn("Attempted to blacklist an already expired token ({}): {}", type, eje.getMessage());
        } catch (DataIntegrityViolationException dive) {
            log.error("Database constraint violation while saving blacklisted token. Token: {} {}", type, token, dive);
            throw new TokenValidationException("Error saving blacklisted token due to DB constraints");
        } catch (Exception e) {
            log.error("Unexpected error during token blacklisting process. Token: {} {}", type, token, e);
            throw new TokenValidationException("Error blacklisting token: " + e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupExpiredTokens() {
        log.info("Starting scheduled cleanup of expired blacklisted tokens.");
        try {
            Instant now = Instant.now();
            int deletedCount = blacklistedTokenRepository.deleteExpiredTokens(now);
            log.info("Cleaned up {} expired blacklisted tokens older than {}", deletedCount, now);
        } catch (Exception e) {
            log.error("Error during scheduled cleanup of expired tokens: {}", e.getMessage(), e);
        }
    }

    public String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Attempted to extract token from invalid header: {}", authHeader);
            throw new TokenValidationException("Invalid or missing Authorization header (must start with Bearer )");
        }
        return authHeader.substring(7);
    }
}
