package za.ac.eyetv.usersercurity.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
*@author: Rethabile Ntsekhe
*@date: @date: 09-04-2025
*/
@Entity
@Table(name = "blacklisted_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class BlacklistedToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private String userEmail;

    @CreationTimestamp
    private Instant blacklistedAt;

    private Instant expiryDate;
}