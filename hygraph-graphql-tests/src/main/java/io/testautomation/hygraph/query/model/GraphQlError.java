package io.testautomation.hygraph.query.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

public record GraphQlError(
        String message,
        List<JsonNode> path,
        List<Location> locations,
        Map<String, JsonNode> extensions) {
    public record Location(int line, int column) {
    }
}
