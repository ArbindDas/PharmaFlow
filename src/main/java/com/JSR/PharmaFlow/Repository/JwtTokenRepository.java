

package com.JSR.PharmaFlow.Repository;

import com.JSR.PharmaFlow.Events.JwtToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JwtTokenRepository extends JpaRepository<JwtToken, String> {

    @Modifying
    @Query("DELETE FROM JwtToken t WHERE t.username = :username")
    void deleteAllByUsername(@Param("username") String username);

    @Query("SELECT t FROM JwtToken t WHERE t.username = :username AND t.expiryDate > :now")
    List<JwtToken> findValidTokensByUser(
            @Param("username") String username,
            @Param("now") LocalDateTime now
    );

    List<JwtToken> findByUsername(String username);

    @Modifying
    @Query("DELETE FROM JwtToken t WHERE t.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
}