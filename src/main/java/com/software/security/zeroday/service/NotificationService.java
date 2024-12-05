package com.software.security.zeroday.service;

import com.software.security.zeroday.entity.Validation;
import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@AllArgsConstructor
@Service
public class NotificationService {
    private final JavaMailSender javaMailSender;
    private final String EMAIL = "no-reply@zeroday.com";
    private final String BRAND = "ZeroDay";

    public void sendActivationCodeEmail(Validation validation, int validityDuration, String chronoUnit) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(this.EMAIL);
        message.setTo(validation.getUser().getEmail());
        message.setSubject(this.BRAND + " - Activation Code");

        String text = "Here is the activation code to create your " + this.BRAND + " account\n"
            + validation.getCode()
            + "\nThis code is only valid for " + validityDuration + " " + chronoUnit.toLowerCase();
        message.setText(text);

        this.javaMailSender.send(message);
    }

    public void sendPasswordResetEmail(Validation validation, int validityDuration, String chronoUnit) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(this.EMAIL);
        message.setTo(validation.getUser().getEmail());
        message.setSubject(this.BRAND + " - Password Reset Code");

        String text = "Here is the code to reset your " + this.BRAND + " account password\n"
            + validation.getCode()
            + "\nThis code is only valid for " + validityDuration + " " + chronoUnit.toLowerCase();
        message.setText(text);

        this.javaMailSender.send(message);
    }
}
