package com.software.security.zeroday;

import com.software.security.zeroday.entity.User;
import com.software.security.zeroday.entity.enumeration.Gender;
import com.software.security.zeroday.entity.enumeration.Nationality;
import com.software.security.zeroday.entity.enumeration.Role;
import com.software.security.zeroday.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.LocalDate;

@AllArgsConstructor
@EnableScheduling
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@SpringBootApplication
public class ZeroDayApplication implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(ZeroDayApplication.class, args);
    }

    @Override
    public void run(String... args) {
        if(this.userRepository.findByEmail("admin@zeroday.com").isEmpty()) {
            String encryptedPassword = this.passwordEncoder.encode("Z3r0D@y!2024#");
            LocalDate birthday = LocalDate.of(2024, 12, 1);
            Instant now = Instant.now();

            User admin = User.builder()
                .email("admin@zeroday.com")
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
    }
}
