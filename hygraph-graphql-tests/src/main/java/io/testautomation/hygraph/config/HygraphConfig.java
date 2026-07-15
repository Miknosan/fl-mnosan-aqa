package io.testautomation.hygraph.config;

import io.testautomation.core.config.ConfigLoader;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

public record HygraphConfig(URI baseUrl, Duration timeout) {
    public static HygraphConfig load() {
        Properties properties = ConfigLoader.load(
                "hygraph.properties", List.of("hygraph.base-url", "hygraph.timeout-ms"));
        return new HygraphConfig(
                URI.create(ConfigLoader.required(properties, "hygraph.base-url")),
                Duration.ofMillis(Long.parseLong(ConfigLoader.required(properties, "hygraph.timeout-ms"))));
    }
}
