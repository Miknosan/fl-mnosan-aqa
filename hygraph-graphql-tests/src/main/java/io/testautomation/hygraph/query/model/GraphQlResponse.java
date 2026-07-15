package io.testautomation.hygraph.query.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public record GraphQlResponse(JsonNode data, List<GraphQlError> errors) {
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
}
