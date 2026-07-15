package io.testautomation.demoqa.config;

import io.testautomation.core.config.ConfigLoader;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

public record DemoQaConfig(
        URI baseUrl,
        String browser,
        boolean headless,
        double slowMotionMillis,
        Duration timeout,
        int viewportWidth,
        int viewportHeight,
        Path artifactsDirectory) {
    public static DemoQaConfig load() {
        List<String> keys = List.of(
                "demoqa.base-url", "demoqa.browser", "demoqa.headless", "demoqa.slow-mo-ms",
                "demoqa.timeout-ms", "demoqa.viewport-width", "demoqa.viewport-height",
                "demoqa.artifacts-directory");
        Properties properties = ConfigLoader.load("demoqa.properties", keys);
        return new DemoQaConfig(
                URI.create(ConfigLoader.required(properties, "demoqa.base-url")),
                ConfigLoader.required(properties, "demoqa.browser"),
                Boolean.parseBoolean(ConfigLoader.required(properties, "demoqa.headless")),
                Double.parseDouble(ConfigLoader.required(properties, "demoqa.slow-mo-ms")),
                Duration.ofMillis(Long.parseLong(ConfigLoader.required(properties, "demoqa.timeout-ms"))),
                Integer.parseInt(ConfigLoader.required(properties, "demoqa.viewport-width")),
                Integer.parseInt(ConfigLoader.required(properties, "demoqa.viewport-height")),
                Path.of(ConfigLoader.required(properties, "demoqa.artifacts-directory")));
    }
}
