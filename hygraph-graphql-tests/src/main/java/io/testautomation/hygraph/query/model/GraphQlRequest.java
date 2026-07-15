package io.testautomation.hygraph.query.model;

import java.util.Map;

public record GraphQlRequest(String query, Map<String, Object> variables, String operationName) {
    public GraphQlRequest {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("GraphQL query must not be blank");
        }
        variables = variables == null ? Map.of() : Map.copyOf(variables);
    }

    public GraphQlRequest(String query, Map<String, Object> variables) {
        this(query, variables, null);
    }
}
