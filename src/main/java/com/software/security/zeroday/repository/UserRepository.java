package com.software.security.zeroday.repository;

import com.software.security.zeroday.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmailAndEnabled(String email, Boolean enabled);
}

