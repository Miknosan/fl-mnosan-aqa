package io.testautomation.hygraph.query.client;

import io.testautomation.core.http.RestSpecFactory;
import io.testautomation.hygraph.config.HygraphConfig;
import io.testautomation.hygraph.query.model.GraphQlRequest;
import io.testautomation.hygraph.query.model.GraphQlResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

public final class GraphQlClient {
    private final RequestSpecification specification;

    public GraphQlClient(HygraphConfig config) {
        specification = RestSpecFactory.create(config.baseUrl(), config.timeout());
    }

    public Response executeRaw(String query, Map<String, Object> variables) {
        return given().spec(specification).body(new GraphQlRequest(query, variables)).post("");
    }

    public GraphQlResponse execute(String query, Map<String, Object> variables) {
        return executeRaw(query, variables).as(GraphQlResponse.class);
    }
}
