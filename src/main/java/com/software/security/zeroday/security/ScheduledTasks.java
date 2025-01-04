package com.software.security.zeroday.security;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class ScheduledTasks {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostConstruct
    public void adminSignInOnStartup() {
        this.executeAdminSignIn();
    }

    @Scheduled(cron = "0 0 */4 * * *")
    public void adminSignIn() {
        this.executeAdminSignIn();
    }

    private void executeAdminSignIn() {
        String adminEmail = "admin@zeroday.com";
        String adminPassword = "Z3r0D@y!2024#";
        this.authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(adminEmail, adminPassword)
        );
        this.jwtService.generate(adminEmail);
    }
}
