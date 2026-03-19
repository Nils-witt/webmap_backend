package dev.nilswitt.webmap.security;


import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

@Component
@Log4j2
public class JWTTokenComponent {

    private final long EXPIRATION_MS;

    private final UserRepository userRepository;
    private final SecretKey secretKey;
    private PublicKey ssoJWKS = null;

    public JWTTokenComponent(UserRepository userRepository, @Value("${application.security.jwt_secret}") String secret, @Value("${application.security.jwt_expiration_ms:10}") long expirationMs, @Value("${application.openid.jwks}") String jwks) {
        this.userRepository = userRepository;
        this.EXPIRATION_MS = expirationMs;
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        try {
            byte[] data = Base64.getDecoder().decode((jwks.getBytes()));
            X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
            KeyFactory fact = KeyFactory.getInstance("RSA");
            this.ssoJWKS = fact.generatePublic(spec);
            log.info("SSO JWT loaded");
        } catch (Exception e) {
            /* */
        }

    }


    public String generateToken(User user) {
        HashMap<String, Object> claims = new HashMap<>();
        log.debug("Generating token for user {}: claims={}", user.getUsername(), claims);
        return Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .claims(claims)
                .signWith(this.secretKey, Jwts.SIG.HS512)
                .compact();
    }

    public UUID extractUserId(String token) {


        return UUID.fromString(Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject());
    }


    public User getUserFromToken(String token) {
        UUID uuid = extractUserId(token);
        return userRepository.findById(uuid).orElse(null);
    }

    public String getUsernameFromSSOToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(this.ssoJWKS)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("preferred_username", String.class);

        } catch (Exception e) {
            // log.error(e.getMessage(), e);
        }
        return null;
    }

    public Claims getClaimsFromSSOToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(this.ssoJWKS)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (Exception e) {
            // log.error(e.getMessage(), e);
        }
        return null;
    }


}
