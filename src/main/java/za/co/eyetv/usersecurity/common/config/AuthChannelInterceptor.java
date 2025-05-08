package za.co.eyetv.usersecurity.common.config;



import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import za.co.eyetv.usersecurity.auth.security.JwtService;

import java.util.List;

@Component
public class AuthChannelInterceptor implements ChannelInterceptor {
    private final JwtService jwtService;

    public AuthChannelInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if ("CONNECT".equals(accessor.getCommand().name())) {
            // 1. Extract JWT from Authorization header sent during WebSocket handshake
            String jwt = null;
            var authHeaders = accessor.getNativeHeader("Authorization");
            if (authHeaders != null && !authHeaders.isEmpty()) {
                String bearer = authHeaders.get(0);
                if (bearer != null && bearer.startsWith("Bearer ")) {
                    jwt = bearer.substring(7);
                }
            }
            // 2. Validate JWT
            if (jwt == null || !jwtService.isTokenValid(jwt)) {
                throw new IllegalArgumentException("Invalid or missing JWT token in WebSocket CONNECT");
            }
            if (jwt != null && jwtService.isTokenValid(jwt)) {
                String username = jwtService.extractUsername(jwt);
                String role = jwtService.extractUserRole(jwt); // implement this if you want
                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                Authentication auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
                accessor.setUser(auth);
            }
        }
        return message;
    }
}
