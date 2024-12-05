package com.software.security.zeroday.repository;

import com.software.security.zeroday.entity.Validation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

public interface ValidationRepository extends JpaRepository<Validation, Long> {
    @Query("SELECT V FROM Validation V WHERE V.code = :code AND V.user.email = :email")
    Optional<Validation> findUserValidationCode(String email, String code);

    @Modifying
    @Query("UPDATE Validation V SET V.enabled = false WHERE V.user.id = :id")
    void disableValidationCodesByUser(Long id);

    void deleteAllByEnabledOrExpiredAtBefore(Boolean enabled, Instant instant);
}
