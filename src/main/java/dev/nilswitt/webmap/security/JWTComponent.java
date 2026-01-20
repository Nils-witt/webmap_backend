package dev.nilswitt.webmap.security;


import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

@Component
public class JWTComponent {

    private final long EXPIRATION_MS;

    private final UserRepository userRepository;
    private final String SECRET_KEY;
    Logger logger = LogManager.getLogger(JWTComponent.class);

    public JWTComponent(UserRepository userRepository, @Value("${application.security.jwt_secret}") String secret, @Value("${application.security.jwt_expiration_ms:10}") long expirationMs) {
        this.userRepository = userRepository;
        this.SECRET_KEY = secret;
        this.EXPIRATION_MS = expirationMs;

    }


    public String generateToken(User user) {
        HashMap<String, Object> claims = new HashMap<>();

        claims.put("overlays", user.getSecurityGroups().stream()
                .flatMap(s -> s.getOverlays().stream().map(o -> o.getId().toString()))
                .distinct()
                .toList()
        );
        claims.put("is_superuser", user.getSecurityGroups().stream().anyMatch(s -> s.getName().equals("SuperAdmins")));
        claims.put("view_all", user.getAuthorities().stream().anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_MAPOVERLAY_VIEW")));
        logger.debug("Generating token for user {}: claims={}", user.getUsername(), claims);
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .addClaims(claims)
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
