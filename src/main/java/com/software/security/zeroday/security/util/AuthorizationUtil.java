package com.software.security.zeroday.security.util;

import com.software.security.zeroday.entity.User;
import com.software.security.zeroday.entity.enumeration.Role;
import com.software.security.zeroday.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class AuthorizationUtil {
    private final UserRepository userRepository;

    public User verifyAuthorization(Long id) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User dbUser = this.userRepository.findById(id)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if(!dbUser.getId().equals(user.getId()) && !Role.ADMINISTRATOR.equals(user.getRole()))
            throw new AccessDeniedException("Access denied");

        return dbUser;
    }
}
