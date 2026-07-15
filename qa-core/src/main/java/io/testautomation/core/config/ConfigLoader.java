package io.testautomation.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Properties;

public final class ConfigLoader {
    private ConfigLoader() {
    }

    public static Properties load(String classpathResource, Collection<String> keys) {
        Properties properties = classpathProperties(classpathResource);
        ExecutionEnvironment environment = ExecutionEnvironment.current();
        loadEnvironmentProfile(properties, environment);

        for (String key : keys) {
            String environmentValue = System.getenv(environmentName(key));
            if (environmentValue != null && !environmentValue.isBlank()) {
                properties.setProperty(key, environmentValue.trim());
            }

            String systemValue = System.getProperty(key);
            if (systemValue != null && !systemValue.isBlank()) {
                properties.setProperty(key, systemValue.trim());
            }
        }
        return properties;
    }

    public static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing configuration value: " + key
                    + " (environment variable: " + environmentName(key) + ")");
        }
        return value.trim();
    }

    private static Properties classpathProperties(String resource) {
        Properties properties = new Properties();
        try (InputStream stream = ConfigLoader.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IllegalStateException("Classpath resource not found: " + resource);
            }
            properties.load(stream);
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read classpath configuration: " + resource, exception);
        }
    }

    private static void loadEnvironmentProfile(Properties properties, ExecutionEnvironment environment) {
        Path path = environment.resolveProfile(Path.of(System.getProperty("user.dir")));
        try (InputStream stream = Files.newInputStream(path)) {
            properties.load(stream);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read configuration file: " + path.toAbsolutePath(), exception);
        }

        String declaredEnvironment = properties.getProperty(ExecutionEnvironment.SYSTEM_PROPERTY);
        if (declaredEnvironment == null || declaredEnvironment.isBlank()) {
            throw new IllegalStateException("Environment profile must declare "
                    + ExecutionEnvironment.SYSTEM_PROPERTY + ": " + path);
        }
        ExecutionEnvironment profileEnvironment = new ExecutionEnvironment(declaredEnvironment);
        if (!profileEnvironment.equals(environment)) {
            throw new IllegalStateException("Environment profile declares " + profileEnvironment.value()
                    + " but execution requested " + environment.value() + ": " + path);
        }
    }

    private static String environmentName(String key) {
        return key.toUpperCase().replace('.', '_').replace('-', '_');
    }
}
