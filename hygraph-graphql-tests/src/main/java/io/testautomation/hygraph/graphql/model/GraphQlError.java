package io.testautomation.hygraph.graphql.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

public record GraphQlError(
        String message,
        List<JsonNode> path,
        List<Location> locations,
        Map<String, JsonNode> extensions) {
    public GraphQlError {
        path = path == null ? List.of() : List.copyOf(path);
        locations = locations == null ? List.of() : List.copyOf(locations);
        extensions = extensions == null ? Map.of() : Map.copyOf(extensions);
    }

    public record Location(int line, int column) {
    }
}
