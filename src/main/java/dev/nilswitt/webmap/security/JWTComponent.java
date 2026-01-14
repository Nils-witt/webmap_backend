package dev.nilswitt.webmap.security;


import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JWTComponent {

    private static final String SECRET_KEY =
            "my-super-secure-secret-key-for-jwt-signing-12345my-super-secure-secret-key-for-jwt-signing-12345my-super-secure-secret-key-for-jwt-signing-12345my-super-secure-secret-key-for-jwt-signing-12345my-super-secure-secret-key-for-jwt-signing-12345";

    private static final long EXPIRATION_MS = 15 * 60 * 1000;

    private final UserRepository userRepository;

    public JWTComponent(UserRepository userRepository) {
        this.userRepository = userRepository;

    }


    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), SignatureAlgorithm.HS512)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }


    public User getUserFromToken(String token) {
        String username = extractUsername(token);
        return userRepository.findByUsername(username).orElse(null);
    }


}
