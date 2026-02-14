package dev.nilswitt.webmap.records;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OAuthUserInfo(
        String sub,
        String name,
        String preferred_username,
        String email,
        List<String> groups
) {
}
