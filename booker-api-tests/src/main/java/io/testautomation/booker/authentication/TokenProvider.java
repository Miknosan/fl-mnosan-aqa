package io.testautomation.booker.authentication;

import io.testautomation.booker.authentication.client.AuthenticationClient;
import io.testautomation.booker.authentication.model.AuthenticationRequest;
import io.testautomation.booker.authentication.model.AuthenticationResponse;
import io.testautomation.booker.config.BookerConfig;

import java.util.Objects;

public final class TokenProvider {
    private final AuthenticationClient client;
    private final BookerConfig config;
    private volatile String cachedToken;

    public TokenProvider(AuthenticationClient client, BookerConfig config) {
        this.client = Objects.requireNonNull(client, "client");
        this.config = Objects.requireNonNull(config, "config");
    }

    public String token() {
        String token = cachedToken;
        if (token == null) {
            synchronized (this) {
                token = cachedToken;
                if (token == null) {
                    token = requestToken();
                    cachedToken = token;
                }
            }
        }
        return token;
    }

    private String requestToken() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username(config.username())
                .password(config.password())
                .build();
        AuthenticationResponse response = client.authenticate(request).as(AuthenticationResponse.class);
        if (response.getToken() == null || response.getToken().isBlank()) {
            throw new IllegalStateException("Booker authentication did not return a token: " + response.getReason());
        }
        return response.getToken();
    }
}
