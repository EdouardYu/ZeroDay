package com.software.security.zeroday.repository;

import com.software.security.zeroday.entity.Jwt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

public interface JwtRepository extends JpaRepository<Jwt, Long> {
    Optional<Jwt> findByValueAndEnabled(String value, Boolean enabled);

    @Query("SELECT J FROM Jwt J WHERE J.enabled = :enabled AND J.user.id = :id")
    Optional<Jwt> findUserTokenByValidity(Long id, Boolean enabled);

    @Modifying
    @Query("UPDATE Jwt J SET J.enabled = false WHERE J.user.id = :id")
    void disableTokensByUser(Long id);

    void deleteAllByEnabledOrExpiredAtBefore(Boolean enabled, Instant instant);
}