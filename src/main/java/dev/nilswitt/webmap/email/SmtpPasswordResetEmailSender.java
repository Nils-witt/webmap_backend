package dev.nilswitt.webmap.email;

import dev.nilswitt.webmap.records.PasswordResetConfig;
import lombok.extern.log4j.Log4j2;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class SmtpPasswordResetEmailSender {

    private final JavaMailSender mailSender;
    private final PasswordResetConfig passwordResetConfig;

    public SmtpPasswordResetEmailSender(JavaMailSender mailSender, PasswordResetConfig passwordResetConfig) {
        this.mailSender = mailSender;
        this.passwordResetConfig = passwordResetConfig;
    }

    public void sendPasswordResetEmail(String toEmail, String displayName, String resetLink, long ttlMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setFrom(passwordResetConfig.mailFrom());
        message.setSubject("Reset your WebMap password");
        message.setText("Hello " + displayName + ",\n\n"
                + "you requested a password reset for your WebMap account.\n"
                + "Use this one-time link to set a new password:\n\n"
                + resetLink + "\n\n"
                + "The link is valid for " + ttlMinutes + " minutes and can only be used once.\n"
                + "If you did not request this, you can ignore this message.\n");

        mailSender.send(message);
        log.info("Password reset email sent to {}", toEmail);
    }
}

