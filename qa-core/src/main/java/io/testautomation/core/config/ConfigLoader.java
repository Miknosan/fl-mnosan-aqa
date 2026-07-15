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
        loadOptionalFile(properties);

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

    private static void loadOptionalFile(Properties properties) {
        String configuredPath = System.getProperty("config.file");
        if (configuredPath == null || configuredPath.isBlank()) {
            return;
        }

        Path path = Path.of(configuredPath);
        try (InputStream stream = Files.newInputStream(path)) {
            properties.load(stream);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read configuration file: " + path.toAbsolutePath(), exception);
        }
    }

    private static String environmentName(String key) {
        return key.toUpperCase().replace('.', '_').replace('-', '_');
    }
}
