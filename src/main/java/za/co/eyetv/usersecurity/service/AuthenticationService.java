package za.co.eyetv.usersecurity.service;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import za.co.eyetv.usersecurity.model.User;
import za.co.eyetv.usersecurity.exception.EmailAlreadyExistsException;
import za.co.eyetv.usersecurity.exception.InvalidRefreshTokenException;
import za.co.eyetv.usersecurity.exception.PasswordMismatchException;
import za.co.eyetv.usersecurity.exception.UsernameAlreadyExistsException;
import za.co.eyetv.usersecurity.dto.AuthenticationResponse;
import za.co.eyetv.usersecurity.dto.LoginRequest;
import za.co.eyetv.usersecurity.dto.RegisterRequest;
import za.co.eyetv.usersecurity.dto.UserDTO;
import za.co.eyetv.usersecurity.model.enums.Roles;
import za.co.eyetv.usersecurity.repository.UserRepository;
import za.co.eyetv.usersecurity.security.JwtService;
import za.co.eyetv.usersecurity.model.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final RateLimiter rateLimiter = RateLimiter.create(10.0); // 10 requests per second


    @Autowired
    public AuthenticationService(UserRepository userRepository, 
                                PasswordEncoder passwordEncoder, 
                                JwtService jwtService, 
                                AuthenticationManager authenticationManager,
                                CustomUserDetailsService customUserDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.customUserDetailsService = customUserDetailsService;
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
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Roles.USER)
                .active(true)
                .build();

        userRepository.save(user);

        // Create UserPrincipal from the saved user
        UserPrincipal userPrincipal = new UserPrincipal(user);

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", List.of("ROLE_" + user.getRole().name()));


        var jwtToken = jwtService.generateToken(extraClaims, userPrincipal);
        var refreshToken = jwtService.generateRefreshToken(userPrincipal);

        UserDTO userDTO = UserDTO.builder()
                .email(user.getEmail())
                .lastActive(user.getLastActive())
                .username(user.getUsername())
                .roles(List.of("ROLE_" + user.getRole().name()))

                .build();

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .user(userDTO)
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

        UserPrincipal userPrincipal = (UserPrincipal) customUserDetailsService.loadUserByUsername(request.getEmail());
        User user = userPrincipal.getUser();

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", List.of("ROLE_" + user.getRole().name()));


        var jwtToken = jwtService.generateToken(extraClaims, userPrincipal);
        var refreshToken = jwtService.generateRefreshToken(userPrincipal);

        UserDTO userDTO = UserDTO.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .roles(List.of("ROLE_" + user.getRole().name()))
                .lastActive(user.getLastActive())
                .build();

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .user(userDTO)
                .build();
    }

    public AuthenticationResponse refreshToken(String refreshToken) {
        try {
            final String userEmail = jwtService.extractUsername(refreshToken);
            if (userEmail == null) {
                throw new InvalidRefreshTokenException("Invalid refresh token");
            }

            UserPrincipal userPrincipal = (UserPrincipal) customUserDetailsService.loadUserByUsername(userEmail);
            User user = userPrincipal.getUser();

            if (!jwtService.isTokenValid(refreshToken, userPrincipal)) {
                throw new InvalidRefreshTokenException("Invalid refresh token");
            }

            var newToken = jwtService.generateToken(userPrincipal);

            UserDTO userDTO = UserDTO.builder()
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .roles(List.of("ROLE_" + user.getRole().name()))
                    .lastActive(user.getLastActive())
                    .build();

            return AuthenticationResponse.builder()
                    .token(newToken)
                    .refreshToken(refreshToken)
                    .user(userDTO)
                    .build();
        } catch (Exception e) {
            throw new InvalidRefreshTokenException("Invalid refresh token: " + e.getMessage(), e);
        }
    }


}
