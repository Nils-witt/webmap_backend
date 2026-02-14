package dev.nilswitt.webmap.security;

import com.github.scribejava.core.builder.api.DefaultApi20;

public class MutableApi extends DefaultApi20 {

    private final String authorizationBaseUrl;
    private final String accessTokenEndpoint;

    public MutableApi(String authorizationBaseUrl, String accessTokenEndpoint) {
        this.authorizationBaseUrl = authorizationBaseUrl;
        this.accessTokenEndpoint = accessTokenEndpoint;
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return authorizationBaseUrl;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return accessTokenEndpoint;
    }
}
