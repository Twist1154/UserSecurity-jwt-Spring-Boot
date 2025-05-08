package za.co.eyetv.usersecurity.auth.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogoutResponse {
    private String message;
}
