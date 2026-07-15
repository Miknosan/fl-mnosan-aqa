package io.testautomation.booker.config;

import io.testautomation.core.config.ConfigLoader;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

public record BookerConfig(URI baseUrl, String username, String password, Duration timeout) {
    private static final String RESOURCE = "booker.properties";
    private static final String BASE_URL = "booker.base-url";
    private static final String USERNAME = "booker.username";
    private static final String PASSWORD = "booker.password";
    private static final String TIMEOUT = "booker.timeout-ms";

    public static BookerConfig load() {
        Properties properties = ConfigLoader.load(RESOURCE, List.of(BASE_URL, USERNAME, PASSWORD, TIMEOUT));
        URI baseUrl = URI.create(ConfigLoader.required(properties, BASE_URL));
        if (!baseUrl.isAbsolute()) {
            throw new IllegalStateException(BASE_URL + " must be an absolute URI");
        }
        return new BookerConfig(
                baseUrl,
                ConfigLoader.required(properties, USERNAME),
                ConfigLoader.required(properties, PASSWORD),
                Duration.ofMillis(Long.parseLong(ConfigLoader.required(properties, TIMEOUT))));
    }
}
