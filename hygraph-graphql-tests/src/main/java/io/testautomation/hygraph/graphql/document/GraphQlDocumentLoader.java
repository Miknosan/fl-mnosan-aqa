package io.testautomation.hygraph.graphql.document;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class GraphQlDocumentLoader {
    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

    private GraphQlDocumentLoader() {
    }

    public static String load(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            throw new IllegalArgumentException("GraphQL document resource path must not be blank");
        }
        return CACHE.computeIfAbsent(resourcePath, GraphQlDocumentLoader::read);
    }

    private static String read(String resourcePath) {
        try (InputStream stream = GraphQlDocumentLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalStateException("GraphQL document not found: " + resourcePath);
            }
            String document = new String(stream.readAllBytes(), StandardCharsets.UTF_8).trim();
            if (document.isBlank()) {
                throw new IllegalStateException("GraphQL document is blank: " + resourcePath);
            }
            return document;
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read GraphQL document: " + resourcePath, exception);
        }
    }
}
