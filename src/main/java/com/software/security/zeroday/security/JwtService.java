package com.software.security.zeroday.security;

import com.software.security.zeroday.dto.user.TokenDTO;
import com.software.security.zeroday.entity.Jwt;
import com.software.security.zeroday.entity.User;
import com.software.security.zeroday.entity.enumeration.LogAction;
import com.software.security.zeroday.repository.JwtRepository;
import com.software.security.zeroday.service.UserActionLogger;
import com.software.security.zeroday.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class JwtService {
    private final UserService userService;
    private final JwtRepository jwtRepository;
    private final UserActionLogger userActionLogger;

    @Value("${encryption.key}")
    private String ENCRYPTION_KEY;

    private final Map<Long, Object> userLocks = new ConcurrentHashMap<>();

    public TokenDTO generate(String email) {
        User user = this.userService.loadUserByUsername(email);
        synchronized (this.getUserLock(user.getId())) {
            try {
                this.jwtRepository.disableTokensByUser(user.getId());

                long currentTime = System.currentTimeMillis();
                long expirationTime = currentTime + 4 * 60 * 60 * 1000; // 4 hours in milliseconds

                String bearer = this.generateJwt(user, currentTime, expirationTime);

                Jwt jwt = Jwt.builder()
                    .value(bearer)
                    .expiredAt(Instant.ofEpochMilli(expirationTime))
                    .enabled(true)
                    .user(user)
                    .build();

                this.jwtRepository.save(jwt);

                this.userActionLogger.log(LogAction.SIGN_IN, user.getUsername());

                return TokenDTO.builder()
                    .bearer(bearer)
                    .build();
            } finally {
                this.userLocks.remove(user.getId());
            }
        }
    }

    private Object getUserLock(Long userId) {
        return this.userLocks.computeIfAbsent(userId, key -> new Object());
    }

    private String generateJwt(User user, long currentTime, long expirationTime) {
        return Jwts.builder()
            .issuedAt(new Date(currentTime))
            .expiration(new Date(expirationTime))
            .subject(String.valueOf(user.getId()))
            .claim("role", user.getRole().name())
            .signWith(this.getKey())
            .compact();
    }

    public Jwt findTokenByValue(String token) {
        return this.jwtRepository.findByValueAndEnabled(token, true)
            .orElseThrow(() -> new SignatureException("Invalid token"));
    }

    public boolean isTokenExpired(String token) {
        Date expirationDate = this.getExpirationDateFromToken(token);
        return expirationDate.before(new Date());
    }

    public Long extractId(String token) {
        return this.getClaim(token, claims -> Long.valueOf(claims.getSubject()));
    }

    private Date getExpirationDateFromToken(String token) {
        return this.getClaim(token, Claims::getExpiration);
    }

    private <T> T getClaim(String token, Function<Claims, T> function) {
        Claims claims = this.getAllClaims(token);
        return function.apply(claims);
    }

    private Claims getAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(this.getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getKey() {
        byte[] decoder = Decoders.BASE64.decode(this.ENCRYPTION_KEY);
        return Keys.hmacShaKeyFor(decoder);
    }

    public void signOut() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Jwt jwt = this.jwtRepository.findUserTokenByValidity(user.getId(), true)
            .orElseThrow(() -> new SignatureException("Tokens are already disabled"));

        jwt.setEnabled(false);
        this.jwtRepository.save(jwt);

        this.userActionLogger.log(LogAction.SIGN_OUT, user.getUsername());
    }

    @Scheduled(cron = "@daily")
    public void removeUselessTokens() {
        Instant now = Instant.now();
        log.info("Deletion of useless tokens at: {}", now);
        this.jwtRepository.deleteAllByEnabledOrExpiredAtBefore(false, now);
    }
}
