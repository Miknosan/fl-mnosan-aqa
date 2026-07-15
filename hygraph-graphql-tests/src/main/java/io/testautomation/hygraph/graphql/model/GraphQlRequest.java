package io.testautomation.hygraph.graphql.model;

import java.util.Map;

public record GraphQlRequest(String query, Map<String, Object> variables, String operationName) {
    public GraphQlRequest {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("GraphQL query must not be blank");
        }
        variables = variables == null ? Map.of() : Map.copyOf(variables);
        if (operationName != null && operationName.isBlank()) {
            throw new IllegalArgumentException("GraphQL operation name must not be blank");
        }
    }
}
