package io.testautomation.hygraph.graphql.model;

import java.util.List;

public record GraphQlResponse<T>(T data, List<GraphQlError> errors) {
    public GraphQlResponse {
        errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
