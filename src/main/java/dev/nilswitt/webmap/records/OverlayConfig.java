package dev.nilswitt.webmap.records;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record OverlayConfig(
        @Value("${application.overlays.path}") String basePath
) {
}
