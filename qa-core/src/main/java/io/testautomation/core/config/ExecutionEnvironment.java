package io.testautomation.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Pattern;

public record ExecutionEnvironment(String value) {
    public static final String SYSTEM_PROPERTY = "test.environment";
    public static final String ENVIRONMENT_VARIABLE = "TEST_ENVIRONMENT";
    public static final String CONFIG_FILE_PROPERTY = "config.file";
    private static final String PROFILE_DIRECTORY = "config/environments";
    private static final Pattern SAFE_NAME = Pattern.compile("[a-z0-9][a-z0-9-]*");

    public ExecutionEnvironment {
        if (value == null) {
            throw new IllegalArgumentException("Execution environment must not be null");
        }
        value = value.trim().toLowerCase(Locale.ROOT);
        if (!SAFE_NAME.matcher(value).matches()) {
            throw new IllegalArgumentException("Execution environment must match " + SAFE_NAME.pattern());
        }
    }

    public static ExecutionEnvironment current() {
        String configuredValue = System.getProperty(SYSTEM_PROPERTY);
        if (configuredValue == null || configuredValue.isBlank()) {
            configuredValue = System.getenv(ENVIRONMENT_VARIABLE);
        }
        if (configuredValue == null || configuredValue.isBlank()) {
            throw new IllegalStateException("Missing execution environment. Provide -D"
                    + SYSTEM_PROPERTY + "=<name> or " + ENVIRONMENT_VARIABLE);
        }
        return new ExecutionEnvironment(configuredValue);
    }

    public Path catalogProfile(Path projectRoot) {
        Path profile = projectRoot.toAbsolutePath().normalize()
                .resolve(PROFILE_DIRECTORY)
                .resolve(value + ".properties");
        Path resolvedProfile = requireProfile(profile);
        validateProfileIdentity(resolvedProfile);
        return resolvedProfile;
    }

    public Path resolveProfile(Path start) {
        String configuredPath = System.getProperty(CONFIG_FILE_PROPERTY);
        if (configuredPath != null && !configuredPath.isBlank()) {
            Path path = Path.of(configuredPath.trim());
            if (!path.isAbsolute()) {
                path = start.toAbsolutePath().normalize().resolve(path);
            }
            return requireProfile(path.normalize());
        }

        Path current = start.toAbsolutePath().normalize();
        while (current != null) {
            Path candidate = current.resolve(PROFILE_DIRECTORY).resolve(value + ".properties");
            if (Files.isRegularFile(candidate)) {
                return candidate.toAbsolutePath().normalize();
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Environment profile not found: " + PROFILE_DIRECTORY
                + "/" + value + ".properties");
    }

    public String displayName() {
        return value.toUpperCase(Locale.ROOT);
    }

    private static Path requireProfile(Path profile) {
        if (!Files.isRegularFile(profile)) {
            throw new IllegalStateException("Environment profile not found: " + profile.toAbsolutePath());
        }
        return profile.toAbsolutePath().normalize();
    }

    private void validateProfileIdentity(Path profile) {
        Properties properties = new Properties();
        try (InputStream stream = Files.newInputStream(profile)) {
            properties.load(stream);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read environment profile: " + profile, exception);
        }

        String declaredEnvironment = properties.getProperty(SYSTEM_PROPERTY);
        if (declaredEnvironment == null || declaredEnvironment.isBlank()) {
            throw new IllegalStateException("Environment profile must declare " + SYSTEM_PROPERTY + ": " + profile);
        }
        ExecutionEnvironment profileEnvironment = new ExecutionEnvironment(declaredEnvironment);
        if (!profileEnvironment.equals(this)) {
            throw new IllegalStateException("Environment profile declares " + profileEnvironment.value()
                    + " but execution requested " + value + ": " + profile);
        }
    }
}
