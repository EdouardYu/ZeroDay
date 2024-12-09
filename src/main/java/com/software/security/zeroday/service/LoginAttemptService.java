package com.software.security.zeroday.service;

import com.software.security.zeroday.entity.LoginAttempt;
import com.software.security.zeroday.repository.LoginAttemptRepository;
import com.software.security.zeroday.service.exception.ToManyAttemptsException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Transactional
@AllArgsConstructor
@Service
public class LoginAttemptService {
    private final LoginAttemptRepository loginAttemptRepository;

    private final int MAX_ATTEMPTS = 5;
    private final int BLOCK_DURATION = 15;
    private final ChronoUnit CHRONO_UNIT = ChronoUnit.MINUTES;
    private final String COOKIE_NAME = "device_id";

    public void validateLoginAttempt(String email, String deviceId) {
        LoginAttempt attempt = this.findOrCreateLoginAttempt(email, deviceId);
        Instant now = Instant.now();

        if (attempt.getAttempts() >= MAX_ATTEMPTS) {
            if (attempt.getLastAttemptAt().plus(BLOCK_DURATION, this.CHRONO_UNIT).isAfter(now)) {
                throw new ToManyAttemptsException("Too many login attempts. Try again later in " +
                    this.BLOCK_DURATION + " " + this.CHRONO_UNIT.name().toLowerCase());
            } else {
                attempt.setAttempts(0);
                attempt.setLastAttemptAt(now);
                this.loginAttemptRepository.save(attempt);
            }
        }
    }

    public int recordFailedAttempt(String email, String deviceId) {
        LoginAttempt attempt = this.findOrCreateLoginAttempt(email, deviceId);
        attempt.setAttempts(attempt.getAttempts() + 1);
        attempt.setLastAttemptAt(Instant.now());
        this.loginAttemptRepository.save(attempt);

        if(attempt.getAttempts() >= this.MAX_ATTEMPTS)
            throw new ToManyAttemptsException("Too many login attempts. Try again later in " +
                this.BLOCK_DURATION + " " + this.CHRONO_UNIT.name().toLowerCase());

        return this.MAX_ATTEMPTS - attempt.getAttempts();
    }

    public void resetAttempts(String email, String deviceId) {
        loginAttemptRepository.deleteByEmailAndDeviceId(email, deviceId);
    }

    private LoginAttempt findOrCreateLoginAttempt(String email, String deviceId) {
        return this.loginAttemptRepository.findByEmailAndDeviceId(email, deviceId)
            .orElseGet(() -> LoginAttempt.builder()
                .email(email)
                .deviceId(deviceId)
                .attempts(0)
                .lastAttemptAt(Instant.now())
                .build()
            );
    }

    public String getDeviceIdentifier(HttpServletRequest request, HttpServletResponse response) {
        String deviceId = this.getDeviceIdFromCookie(request);

        if (deviceId == null) {
            deviceId = this.generateUniqueDeviceId();
            this.addDeviceIdCookie(response, deviceId);
        }

        return deviceId;
    }

    private String getDeviceIdFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        return Stream.of(request.getCookies())
            .filter(cookie -> this.COOKIE_NAME.equals(cookie.getName()))
            .map(Cookie::getValue)
            .findAny()
            .orElse(null);
    }

    private void addDeviceIdCookie(HttpServletResponse response, String deviceId) {
        Cookie cookie = new Cookie(this.COOKIE_NAME, deviceId);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(60 * 60 * 24 * 365); // 1 year
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private String generateUniqueDeviceId() {
        String uniqueDeviceId;
        do uniqueDeviceId = UUID.randomUUID().toString();
        while (this.loginAttemptRepository.existsByDeviceId(uniqueDeviceId));

        return uniqueDeviceId;
    }

    @Scheduled(cron = "@daily")
    public void removeUselessLoginAttempts() {
        Instant now = Instant.now();
        log.info("Deletion of useless login attempts at: {}", now);
        this.loginAttemptRepository.deleteAllByLastAttemptAtBefore(
            now.minus(this.BLOCK_DURATION, this.CHRONO_UNIT) // 15 minutes after last attempt
        );
    }
}
