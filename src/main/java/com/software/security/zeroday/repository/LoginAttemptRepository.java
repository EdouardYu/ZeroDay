package com.software.security.zeroday.repository;

import com.software.security.zeroday.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    Optional<LoginAttempt> findByEmailAndDeviceId(String email, String deviceId);

    void deleteByEmailAndDeviceId(String email, String deviceId);

    boolean existsByDeviceId(String uniqueDeviceId);

    void deleteAllByLastAttemptAtBefore(Instant instant);
}