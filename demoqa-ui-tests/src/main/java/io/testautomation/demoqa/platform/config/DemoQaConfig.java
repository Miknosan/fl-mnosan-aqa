package io.testautomation.demoqa.platform.config;

import io.testautomation.core.config.ConfigLoader;
import io.testautomation.core.config.ExecutionEnvironment;

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
        Path artifactsDirectory,
        String locale,
        String timezone,
        boolean traceEnabled,
        boolean blockThirdPartyContent) {
    public static DemoQaConfig load() {
        ExecutionEnvironment environment = ExecutionEnvironment.current();
        List<String> keys = List.of(
                "demoqa.base-url", "demoqa.browser", "demoqa.headless", "demoqa.slow-mo-ms",
                "demoqa.timeout-ms", "demoqa.viewport-width", "demoqa.viewport-height",
                "demoqa.artifacts-directory", "demoqa.locale", "demoqa.timezone",
                "demoqa.trace-enabled", "demoqa.block-third-party-content");
        Properties properties = ConfigLoader.load("demoqa.properties", keys);
        DemoQaConfig config = new DemoQaConfig(
                URI.create(ConfigLoader.required(properties, "demoqa.base-url")),
                ConfigLoader.required(properties, "demoqa.browser"),
                Boolean.parseBoolean(ConfigLoader.required(properties, "demoqa.headless")),
                Double.parseDouble(ConfigLoader.required(properties, "demoqa.slow-mo-ms")),
                Duration.ofMillis(Long.parseLong(ConfigLoader.required(properties, "demoqa.timeout-ms"))),
                Integer.parseInt(ConfigLoader.required(properties, "demoqa.viewport-width")),
                Integer.parseInt(ConfigLoader.required(properties, "demoqa.viewport-height")),
                Path.of(ConfigLoader.required(properties, "demoqa.artifacts-directory"))
                        .resolve(environment.value()),
                ConfigLoader.required(properties, "demoqa.locale"),
                ConfigLoader.required(properties, "demoqa.timezone"),
                Boolean.parseBoolean(ConfigLoader.required(properties, "demoqa.trace-enabled")),
                Boolean.parseBoolean(ConfigLoader.required(properties, "demoqa.block-third-party-content")));
        config.validate();
        return config;
    }

    private void validate() {
        if (!List.of("http", "https").contains(baseUrl.getScheme())) {
            throw new IllegalStateException("demoqa.base-url must use http or https");
        }
        BrowserEngineValidator.validate(browser);
        if (slowMotionMillis < 0) {
            throw new IllegalStateException("demoqa.slow-mo-ms must not be negative");
        }
        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalStateException("demoqa.timeout-ms must be positive");
        }
        if (viewportWidth < 800 || viewportHeight < 600) {
            throw new IllegalStateException("DemoQA viewport must be at least 800x600");
        }
        if (locale.isBlank() || timezone.isBlank()) {
            throw new IllegalStateException("DemoQA locale and timezone must not be blank");
        }
    }

    private static final class BrowserEngineValidator {
        private static void validate(String value) {
            if (!List.of("chromium", "firefox", "webkit").contains(value.toLowerCase())) {
                throw new IllegalStateException("demoqa.browser must be chromium, firefox, or webkit");
            }
        }
    }
}
