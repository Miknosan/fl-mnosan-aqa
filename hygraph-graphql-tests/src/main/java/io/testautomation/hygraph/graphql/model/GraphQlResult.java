package io.testautomation.hygraph.graphql.model;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record GraphQlResult<T>(
        String operationName,
        int statusCode,
        String contentType,
        Map<String, List<String>> headers,
        Duration elapsedTime,
        String rawBody,
        GraphQlResponse<T> body) {
    public GraphQlResult {
        if (operationName == null || operationName.isBlank()) {
            throw new IllegalArgumentException("GraphQL operation name must not be blank");
        }
        if (statusCode < 100 || statusCode > 599) {
            throw new IllegalArgumentException("Invalid HTTP status code: " + statusCode);
        }
        contentType = contentType == null ? "" : contentType;
        headers = immutableHeaders(headers);
        if (elapsedTime == null || elapsedTime.isNegative()) {
            throw new IllegalArgumentException("GraphQL elapsed time must not be null or negative");
        }
        rawBody = rawBody == null ? "" : rawBody;
        if (body == null) {
            throw new IllegalArgumentException("GraphQL response body must not be null");
        }
    }

    private static Map<String, List<String>> immutableHeaders(Map<String, List<String>> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        Map<String, List<String>> copy = new LinkedHashMap<>();
        source.forEach((name, values) -> copy.put(name, values == null ? List.of() : List.copyOf(values)));
        return Map.copyOf(copy);
    }
}
