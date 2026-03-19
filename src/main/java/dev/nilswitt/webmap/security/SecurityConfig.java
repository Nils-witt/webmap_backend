package dev.nilswitt.webmap.security;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import dev.nilswitt.webmap.base.ui.views.LoginView;
import dev.nilswitt.webmap.security.filter.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }


    /**
     * Controls access to the UI
     *
     * @param http
     * @return
     * @throws Exception
     */
    @Order(2)
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Configure Vaadin's security using VaadinSecurityConfigurer
        http.securityMatcher("/ui/**", "/login", "/forgot-password", "/reset-password", "/", "/VAADIN/**").with(VaadinSecurityConfigurer.vaadin(), configurer -> {
            configurer.loginView(LoginView.class, "/");
            configurer.defaultSuccessUrl("/ui");
        });

        return http.build();
    }

    /**
     * Controls the access to the Websocket
     * @param http
     * @return
     * @throws Exception
     */
    @Order(0)
    @Bean
    SecurityFilterChain wsFilterChain(HttpSecurity http) throws Exception {

        return http.securityMatcher("/api/ws/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }


    /**
     * Controlls the access to the REST API
     * @param http
     * @return
     * @throws Exception
     */
    @Order(1)
    @Bean
    SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        return http.securityMatcher("/api/**")
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/api/token/**").permitAll();
                    auth.anyRequest().authenticated();
                })
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
