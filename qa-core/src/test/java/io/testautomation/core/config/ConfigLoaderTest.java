package io.testautomation.core.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigLoaderTest {
    private static final String BASE_URL = "service.base-url";
    private static final String TIMEOUT = "service.timeout-ms";

    @TempDir
    Path temporaryDirectory;

    @AfterEach
    void clearSystemProperties() {
        System.clearProperty(ExecutionEnvironment.SYSTEM_PROPERTY);
        System.clearProperty(ExecutionEnvironment.CONFIG_FILE_PROPERTY);
        System.clearProperty(BASE_URL);
        System.clearProperty(TIMEOUT);
    }

    @Test
    void shouldApplyConfigurationInTheRequiredPrecedence() throws IOException {
        Path profile = profile("dev", "service.base-url=https://dev.example.test\nservice.timeout-ms=2000\n");
        System.setProperty(ExecutionEnvironment.SYSTEM_PROPERTY, "dev");
        System.setProperty(ExecutionEnvironment.CONFIG_FILE_PROPERTY, profile.toString());
        System.setProperty(TIMEOUT, "3000");

        Properties properties = ConfigLoader.load("config-defaults.properties", List.of(BASE_URL, TIMEOUT));

        assertEquals("https://dev.example.test", properties.getProperty(BASE_URL));
        assertEquals("3000", properties.getProperty(TIMEOUT));
    }

    @Test
    void shouldRejectProfileForAnotherEnvironment() throws IOException {
        Path profile = profile("stage", "service.base-url=https://stage.example.test\n");
        System.setProperty(ExecutionEnvironment.SYSTEM_PROPERTY, "dev");
        System.setProperty(ExecutionEnvironment.CONFIG_FILE_PROPERTY, profile.toString());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ConfigLoader.load("config-defaults.properties", List.of(BASE_URL)));

        assertTrue(exception.getMessage().contains("declares stage"));
        assertTrue(exception.getMessage().contains("requested dev"));
    }

    @Test
    void shouldRequireEnvironmentProfileIdentity() throws IOException {
        Path profile = temporaryDirectory.resolve("profile.properties");
        Files.writeString(profile, "service.base-url=https://dev.example.test\n");
        System.setProperty(ExecutionEnvironment.SYSTEM_PROPERTY, "dev");
        System.setProperty(ExecutionEnvironment.CONFIG_FILE_PROPERTY, profile.toString());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ConfigLoader.load("config-defaults.properties", List.of(BASE_URL)));

        assertTrue(exception.getMessage().contains(ExecutionEnvironment.SYSTEM_PROPERTY));
    }

    private Path profile(String environment, String configuration) throws IOException {
        Path profile = temporaryDirectory.resolve(environment + ".properties");
        Files.writeString(profile, "test.environment=" + environment + "\n" + configuration);
        return profile;
    }
}
