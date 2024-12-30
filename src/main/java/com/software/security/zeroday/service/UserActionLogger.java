package com.software.security.zeroday.service;

import com.software.security.zeroday.entity.enumeration.LogAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserActionLogger {
    private static final Logger logger = LoggerFactory.getLogger("USER_LOGS");
    public void log(LogAction action, String username) {
        logger.info("User: {}, Action: {}", username, action.name());
    }
}
