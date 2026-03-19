package dev.nilswitt.webmap.security.filter;

import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.SecurityGroupRepository;
import dev.nilswitt.webmap.entities.repositories.UserRepository;
import dev.nilswitt.webmap.security.JWTTokenComponent;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Log4j2
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JWTTokenComponent jwtUtil;
    private final UserRepository userRepository;
    private final SecurityGroupRepository securityGroupRepository;

    public JwtFilter(JWTTokenComponent jwtUtil, UserRepository userRepository, SecurityGroupRepository securityGroupRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.securityGroupRepository = securityGroupRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            if (request.getParameter("token") != null) {
                token = request.getParameter("token");
            }
        }

        if (token != null && !token.isBlank()) {
            try {
                User user = jwtUtil.getUserFromToken(token);

                AbstractAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, token, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                log.warn("Local JWT validation failed: {}", e.getMessage());
            }
            try {
                User user = null;

                String username = jwtUtil.getUsernameFromSSOToken(token);
                Optional<User> userOpt = userRepository.findByUsername(username);
                Claims claims = jwtUtil.getClaimsFromSSOToken(token);

                if (userOpt.isPresent()) {
                    User newUser = userOpt.get();
                    ArrayList<String> cGroups = (ArrayList<String>) claims.get("groups", ArrayList.class);
                    Set<SecurityGroup> securityGroups = newUser.getSecurityGroups();
                    cGroups.forEach(cGroup -> {
                        List<SecurityGroup> sg = securityGroupRepository.findBySsoGroupName(cGroup);
                        securityGroups.addAll(sg);
                    });

                    newUser.setSecurityGroups(securityGroups);
                    user = userRepository.save(newUser);
                } else {
                    User newUser = new User();
                    newUser.setUsername(username);
                    newUser.setEmail(claims.get("email", String.class));
                    newUser.setFirstName(claims.get("given_name", String.class));
                    newUser.setLastName(claims.get("name", String.class));
                    ArrayList<String> cGroups = (ArrayList<String>) claims.get("groups", ArrayList.class);
                    Set<SecurityGroup> securityGroups = newUser.getSecurityGroups();
                    cGroups.forEach(cGroup -> {
                        List<SecurityGroup> sg = securityGroupRepository.findBySsoGroupName(cGroup);
                        securityGroups.addAll(sg);
                    });

                    newUser.setSecurityGroups(securityGroups);
                    user = userRepository.save(newUser);
                }


                AbstractAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, token, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (Exception e) {
                log.debug("SSO JWT validation failed: {}", e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}
