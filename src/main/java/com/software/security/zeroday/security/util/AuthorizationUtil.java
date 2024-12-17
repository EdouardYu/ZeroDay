package com.software.security.zeroday.security.util;

import com.software.security.zeroday.entity.User;
import com.software.security.zeroday.entity.enumeration.Role;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class AuthorizationUtil {
    public User verifyAuthorization(Long id) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!id.equals(user.getId()) && !Role.ADMINISTRATOR.equals(user.getRole()))
            throw new AccessDeniedException("Access denied");

        return user;
    }
}
