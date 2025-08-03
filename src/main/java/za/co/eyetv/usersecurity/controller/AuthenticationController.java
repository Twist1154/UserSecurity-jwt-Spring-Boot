package za.co.eyetv.usersecurity.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import za.co.eyetv.usersecurity.service.AuthenticationService;
import za.co.eyetv.usersecurity.service.LogoutService;
import za.co.eyetv.usersecurity.dto.*;

/**
 * @author: Rethabile Ntsekhe
 * @date: @date: 09-04-2025
 */
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final LogoutService logoutService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService,
                                    LogoutService logoutService) {
        this.authenticationService = authenticationService;
        this.logoutService = logoutService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        log.info("Registering new user: {}", request);
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        log.info("Logging in user: {}", request);
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        log.info("Refreshing token: {}", request);
        return ResponseEntity.ok(authenticationService.refreshToken(request.getRefreshToken()));
    }


    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@Valid @RequestBody LogoutRequest logoutRequest) {
        log.info("Logging out user with provided token: '{}'", logoutRequest.getToken());
        // Pass the token from the DTO to the service

        log.info("LogoutRequest created with token: {}", logoutRequest);
        log.info("Received token for logout: '{}'", logoutRequest.getToken());
        LogoutResponse response = logoutService.logout(
                logoutRequest.getToken(),
                logoutRequest.getRefreshToken()
        );
        return ResponseEntity.ok(response);
    }
}