package com.software.security.zeroday.security;

import com.software.security.zeroday.entity.User;
import com.software.security.zeroday.entity.enumeration.Gender;
import com.software.security.zeroday.entity.enumeration.Nationality;
import com.software.security.zeroday.entity.enumeration.Role;
import com.software.security.zeroday.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;

@AllArgsConstructor
@Component
public class ScheduledTasks {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String EMAIL = "admin@zeroday.com";
    private final String PASSWORD = "Z3r0D@y!2024#";

    @PostConstruct
    public void adminSignInOnStartup() {
        if(this.userRepository.findByEmail(this.EMAIL).isEmpty()) {
            String encryptedPassword = this.passwordEncoder.encode(this.PASSWORD);
            LocalDate birthday = LocalDate.of(2024, 12, 1);
            Instant now = Instant.now();

            User admin = User.builder()
                .email(this.EMAIL)
                .password(encryptedPassword)
                .firstname("Admin")
                .lastname("ZeroDay")
                .username("Admin ZeroDay")
                .birthday(birthday)
                .gender(Gender.UNSPECIFIED)
                .nationality(Nationality.UNSPECIFIED)
                .bio("Hey! I'm ZeroDay Administrator.")
                .createdAt(now)
                .updatedAt(now)
                .enabled(true)
                .role(Role.ADMINISTRATOR)
                .build();

            this.userRepository.save(admin);
        }

        this.executeAdminSignIn();
    }

    @Scheduled(cron = "0 0 */4 * * *")
    public void adminSignIn() {
        this.executeAdminSignIn();
    }

    private void executeAdminSignIn() {
        this.authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(this.EMAIL, this.PASSWORD)
        );
        this.jwtService.generate(this.EMAIL);
    }
}
