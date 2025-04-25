package za.ac.eyetv.usersercurity.service;

import com.google.common.util.concurrent.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import za.ac.eyetv.usersercurity.exception.*;
import za.ac.eyetv.usersercurity.model.dto.AuthenticationResponse;
import za.ac.eyetv.usersercurity.model.dto.LoginRequest;
import za.ac.eyetv.usersercurity.model.dto.LogoutResponse;
import za.ac.eyetv.usersercurity.model.dto.RegisterRequest;
import za.ac.eyetv.usersercurity.model.enums.Roles;
import za.ac.eyetv.usersercurity.model.User;
import za.ac.eyetv.usersercurity.repository.UserRepository;
import za.ac.eyetv.usersercurity.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;

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

        var jwtToken = jwtService.generateToken(user);
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
        // Implement this functionality if needed
        /*if (!user.isActive()) {
            throw new UserNotActivatedException("Your account is not activated.");
        }*/

        var jwtToken = jwtService.generateToken(user);
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