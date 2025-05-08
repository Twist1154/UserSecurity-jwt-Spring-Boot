package za.co.eyetv.usersecurity.blacklist.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import za.co.eyetv.usersecurity.common.model.enums.TokenType;

import java.time.Instant;

/**
*@author: Rethabile Ntsekhe
*@date: @date: 09-04-2025
*/
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "blacklisted_tokens")
public class BlacklistedToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType tokenType;

    @Column(nullable = false)
    private String userEmail;

    @CreationTimestamp
    private Instant blacklistedAt;

    private Instant expiryDate;
}