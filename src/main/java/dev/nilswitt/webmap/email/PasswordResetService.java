package dev.nilswitt.webmap.email;

import dev.nilswitt.webmap.entities.PasswordResetToken;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.PasswordResetTokenRepository;
import dev.nilswitt.webmap.entities.repositories.UserRepository;
import dev.nilswitt.webmap.exceptions.InvalidPasswordResetTokenException;
import dev.nilswitt.webmap.records.PasswordResetConfig;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Log4j2
@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final SmtpPasswordResetEmailSender passwordResetEmailSender;
    private final PasswordResetConfig passwordResetConfig;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository passwordResetTokenRepository,
                                PasswordEncoder passwordEncoder,
                                SmtpPasswordResetEmailSender passwordResetEmailSender,
                                PasswordResetConfig passwordResetConfig) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetEmailSender = passwordResetEmailSender;
        this.passwordResetConfig = passwordResetConfig;
    }

    @Transactional
    public void requestPasswordReset(String email) {
        Optional<User> optionalUser = this.userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return;
        }

        User user = optionalUser.get();
        this.passwordResetTokenRepository.deleteByUser(user);

        String rawToken = generateRawToken();
        String tokenHash = sha256(rawToken);

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setUser(user);
        passwordResetToken.setTokenHash(tokenHash);
        passwordResetToken.setExpiresAt(Instant.now().plusSeconds(this.passwordResetConfig.ttlMinutes() * 60));
        this.passwordResetTokenRepository.save(passwordResetToken);

        String resetLink = buildResetLink(rawToken);
        try {
            this.passwordResetEmailSender.sendPasswordResetEmail(user.getEmail(), user.getDisplayName(), resetLink, this.passwordResetConfig.ttlMinutes());
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", user.getEmail(), e.getMessage(), e);
        }
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken token = this.passwordResetTokenRepository.findByTokenHash(sha256(rawToken))
                .orElseThrow(() -> new InvalidPasswordResetTokenException("Invalid reset token."));

        if (token.isExpired()) {
            throw new InvalidPasswordResetTokenException("Reset token has expired.");
        }

        User user = token.getUser();
        user.setPassword(this.passwordEncoder.encode(newPassword));
        this.userRepository.save(user);

        this.passwordResetTokenRepository.save(token);
        this.passwordResetTokenRepository.deleteByUser(user);
    }

    private String buildResetLink(String rawToken) {
        return this.passwordResetConfig.baseUrl() + "/reset-password?token=" + rawToken;
    }

    private static String generateRawToken() {
        byte[] tokenBytes = new byte[32];
        new java.security.SecureRandom().nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) {
                    hex.append('0');
                }
                hex.append(h);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}


