package dev.nilswitt.webmap.records;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record ApplicationInfo(@Value("${spring.application.version:unknown}") String version) {
}
