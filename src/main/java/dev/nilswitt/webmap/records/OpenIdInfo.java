package dev.nilswitt.webmap.records;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record OpenIdInfo(
        @Value("${application.openid.base-url:unknown}") String baseUrl,
        @Value("${application.openid.token-url:unknown}") String tokenUrl,
        @Value("${application.openid.client-id:unknown}") String clientId,
        @Value("${application.openid.callbackUrl:unknown}") String callbackUrl,
        @Value("${application.openid.client-secret:unknown}") String clientSecret,
        @Value("${application.openid.userinfo-url:unknown}") String userInfoUrl,
        @Value("${application.openid.scopes:unknown}") String scopes
) {
}
