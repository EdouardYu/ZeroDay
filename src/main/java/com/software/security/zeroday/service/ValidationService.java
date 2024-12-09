package com.software.security.zeroday.service;

import com.software.security.zeroday.entity.User;
import com.software.security.zeroday.entity.Validation;
import com.software.security.zeroday.repository.ValidationRepository;
import com.software.security.zeroday.service.exception.ValidationCodeException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Transactional
@AllArgsConstructor
@Service
public class ValidationService {
    private final ValidationRepository validationRepository;
    private final NotificationService notificationService;
    private final int VALIDITY_DURATION = 10;
    private final ChronoUnit CHRONO_UNIT = ChronoUnit.MINUTES;

    private final Map<Long, Object> userLocks = new ConcurrentHashMap<>();

    public void register(User user) {
        synchronized (this.getUserLock(user.getId())) {
            try {
                this.validationRepository.disableValidationCodesByUser(user.getId());
                Validation validation = this.generateValidationCode(user);
                this.notificationService.sendActivationCodeEmail(validation, this.VALIDITY_DURATION, this.CHRONO_UNIT.name().toLowerCase());
            } finally {
                this.userLocks.remove(user.getId());
            }
        }
    }

    public void resetPassword(User user) {
        synchronized (this.getUserLock(user.getId())) {
            try {
                this.validationRepository.disableValidationCodesByUser(user.getId());
                Validation validation = this.generateValidationCode(user);
                this.notificationService.sendPasswordResetEmail(validation, this.VALIDITY_DURATION, this.CHRONO_UNIT.name().toLowerCase());
            } finally {
                this.userLocks.remove(user.getId());
            }
        }
    }

    private Object getUserLock(Long userId) {
        return this.userLocks.computeIfAbsent(userId, key -> new Object());
    }

    private Validation generateValidationCode(User user) {
        Random random = new Random();
        int randomInteger = random.nextInt(1_000_000);
        String code = String.format("%06d", randomInteger);

        Validation validation = Validation.builder()
            .code(code)
            .expiredAt(Instant.now().plus(this.VALIDITY_DURATION, this.CHRONO_UNIT))
            .enabled(true)
            .user(user)
            .build();

        return this.validationRepository.save(validation);
    }

    public Validation findUserActivationCode(String email, String activationCode) {
        return this.validationRepository.findUserValidationCode(email, activationCode)
            .orElseThrow(() -> new ValidationCodeException("Invalid activation code"));
    }

    public Validation findUserPasswordResetCode(String email, String passwordResetCode) {
        return this.validationRepository.findUserValidationCode(email, passwordResetCode)
            .orElseThrow(() -> new ValidationCodeException("Invalid password reset code"));
    }

    @Scheduled(cron = "@daily")
    public void removeUselessValidationCodes() {
        Instant now = Instant.now();
        log.info("Deletion of expired validation codes at: {}", now);
        this.validationRepository.deleteAllByEnabledOrExpiredAtBefore(
            false,
            now.minus(1, ChronoUnit.DAYS) // 1 day after validation code expires
        );
    }
}
