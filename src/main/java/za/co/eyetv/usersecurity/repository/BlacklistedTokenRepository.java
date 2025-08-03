package za.co.eyetv.usersecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import za.co.eyetv.usersecurity.model.BlacklistedToken;
import za.co.eyetv.usersecurity.model.enums.TokenType;

import java.time.Instant;
import java.util.List;

/**
*@author: Rethabile Ntsekhe
*@date: @date: 09-04-2025
*/
@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {
    boolean existsByToken(String token);

    @Query("DELETE FROM BlacklistedToken b WHERE b.expiryDate < :now")
    int deleteExpiredTokens(Instant now);

    boolean existsByTokenAndTokenType(String token, TokenType tokenType);
    List<BlacklistedToken> findByExpiryDateBefore(Instant now);
}
