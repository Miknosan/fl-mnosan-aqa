package io.testautomation.hygraph.config;

import io.testautomation.core.config.ConfigLoader;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

public record HygraphConfig(URI baseUrl, Duration timeout) {
    public HygraphConfig {
        if (baseUrl == null || !baseUrl.isAbsolute()) {
            throw new IllegalArgumentException("Hygraph base URL must be absolute");
        }
        String scheme = baseUrl.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("Hygraph base URL must use HTTP or HTTPS");
        }
        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("Hygraph timeout must be positive");
        }
    }

    public static HygraphConfig load() {
        Properties properties = ConfigLoader.load(
                "hygraph.properties", List.of("hygraph.base-url", "hygraph.timeout-ms"));
        return new HygraphConfig(
                URI.create(ConfigLoader.required(properties, "hygraph.base-url")),
                Duration.ofMillis(Long.parseLong(ConfigLoader.required(properties, "hygraph.timeout-ms"))));
    }
}
