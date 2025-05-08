package za.co.eyetv.usersecurity.auth.service;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import za.co.eyetv.usersecurity.auth.exception.EmailAlreadyExistsException;
import za.co.eyetv.usersecurity.auth.exception.InvalidRefreshTokenException;
import za.co.eyetv.usersecurity.common.exception.PasswordMismatchException;
import za.co.eyetv.usersecurity.user.exception.UsernameAlreadyExistsException;
import za.co.eyetv.usersecurity.auth.dto.AuthenticationResponse;
import za.co.eyetv.usersecurity.auth.dto.LoginRequest;
import za.co.eyetv.usersecurity.auth.dto.RegisterRequest;
import za.co.eyetv.usersecurity.common.model.enums.Roles;
import za.co.eyetv.usersecurity.user.model.User;
import za.co.eyetv.usersecurity.user.repository.UserRepository;
import za.co.eyetv.usersecurity.auth.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RateLimiter rateLimiter = RateLimiter.create(10.0); // 10 requests per second


    @Autowired
    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException(request.getUsername());
        }

        var user = User.builder()
                //.name(request.getName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Roles.USER)
                .active(true)
                .build();

        userRepository.save(user);

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());

        var jwtToken = jwtService.generateToken(extraClaims, user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    public AuthenticationResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new PasswordMismatchException("Incorrect email or password.");
        }

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());

        var jwtToken = jwtService.generateToken(extraClaims, user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    public AuthenticationResponse refreshToken(String refreshToken) {
        try {
            final String userEmail = jwtService.extractUsername(refreshToken);
            if (userEmail == null) {
                throw new InvalidRefreshTokenException("Invalid refresh token");
            }

            var user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            if (!jwtService.isTokenValid(refreshToken, user)) {
                throw new InvalidRefreshTokenException("Invalid refresh token");
            }

            var newToken = jwtService.generateToken(user);

            return AuthenticationResponse.builder()
                    .token(newToken)
                    .refreshToken(refreshToken)
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .role(user.getRole().name())
                    .build();
        } catch (Exception e) {
            throw new InvalidRefreshTokenException("Invalid refresh token: " + e.getMessage(), e);
        }
    }


}