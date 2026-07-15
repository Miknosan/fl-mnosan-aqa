package io.testautomation.core.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecutionEnvironmentTest {
    @TempDir
    Path temporaryDirectory;

    @AfterEach
    void clearSystemProperties() {
        System.clearProperty(ExecutionEnvironment.SYSTEM_PROPERTY);
        System.clearProperty(ExecutionEnvironment.CONFIG_FILE_PROPERTY);
    }

    @Test
    void shouldNormalizeAValidEnvironmentName() {
        ExecutionEnvironment environment = new ExecutionEnvironment(" Stage ");

        assertEquals("stage", environment.value());
        assertEquals("STAGE", environment.displayName());
    }

    @Test
    void shouldRejectUnsafeEnvironmentName() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ExecutionEnvironment("../stage"));

        assertTrue(exception.getMessage().contains("must match"));
    }

    @Test
    void shouldResolveProfileFromAnAncestorCatalog() throws IOException {
        Path profile = temporaryDirectory.resolve("config/environments/dev.properties");
        Files.createDirectories(profile.getParent());
        Files.writeString(profile, "test.environment=dev");
        Path nestedDirectory = Files.createDirectories(temporaryDirectory.resolve("module/target"));

        Path resolved = new ExecutionEnvironment("dev").resolveProfile(nestedDirectory);

        assertEquals(profile.toRealPath(), resolved.toRealPath());
    }

    @Test
    void shouldFailWhenEnvironmentProfileDoesNotExist() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> new ExecutionEnvironment("preprod").resolveProfile(temporaryDirectory));

        assertTrue(exception.getMessage().contains("preprod.properties"));
    }
}
