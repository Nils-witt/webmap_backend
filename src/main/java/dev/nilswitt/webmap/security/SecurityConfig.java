package dev.nilswitt.webmap.security;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import dev.nilswitt.webmap.views.LoginView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.util.AntPathMatcher;

@EnableWebSecurity
@Configuration
class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Order(2)
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Configure Vaadin's security using VaadinSecurityConfigurer
        http.securityMatcher("/ui/**","/login","/", "/VAADIN/**").with(VaadinSecurityConfigurer.vaadin(), configurer -> {
            configurer.loginView(LoginView.class, "/logged-out.html");
            configurer.defaultSuccessUrl("/ui", true);
        });

        return http.build();
    }

    @Order(0)
    @Bean
    SecurityFilterChain wsFilterChain(HttpSecurity http) throws Exception {

        return http.securityMatcher("/ws/**")
                .authorizeHttpRequests(auth -> {
                    auth.anyRequest().permitAll();
                })
                .build();
    }


    @Order(1)
    @Bean
    SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        return http.securityMatcher("/api/**")
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/api/token").permitAll();
                    auth.anyRequest().authenticated();
                })
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
