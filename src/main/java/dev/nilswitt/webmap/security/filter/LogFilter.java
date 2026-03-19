package dev.nilswitt.webmap.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class LogFilter extends OncePerRequestFilter {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    private final boolean loggingEnabled;

    public LogFilter(@Value("${application.logging.requests:true}") boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;

    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        if (loggingEnabled) {
            logger.info("Incoming request: {} {}", request.getMethod(), request.getRequestURI());
        }
        filterChain.doFilter(request, response);
    }
}
