package io.testautomation.hygraph.graphql.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.testautomation.core.http.RestSpecFactory;
import io.testautomation.core.json.ObjectMapperFactory;
import io.testautomation.hygraph.config.HygraphConfig;
import io.testautomation.hygraph.graphql.model.GraphQlRequest;
import io.testautomation.hygraph.graphql.model.GraphQlResponse;
import io.testautomation.hygraph.graphql.model.GraphQlResult;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;

public final class GraphQlClient {
    private final RequestSpecification specification;

    public GraphQlClient(HygraphConfig config) {
        specification = RestSpecFactory.create(config.baseUrl(), config.timeout());
    }

    public <T> GraphQlResult<T> execute(
            GraphQlRequest request,
            TypeReference<GraphQlResponse<T>> responseType) {
        long startedAt = System.nanoTime();
        Response response = given()
                .spec(specification)
                .body(request)
                .post("");
        Duration elapsedTime = Duration.ofNanos(System.nanoTime() - startedAt);
        String rawBody = response.asString();

        try {
            GraphQlResponse<T> body = ObjectMapperFactory.get().readValue(rawBody, responseType);
            return new GraphQlResult<>(
                    request.operationName(),
                    response.statusCode(),
                    response.contentType(),
                    responseHeaders(response),
                    elapsedTime,
                    rawBody,
                    body);
        } catch (JsonProcessingException exception) {
            throw new GraphQlResponseDeserializationException(
                    request.operationName(), response.statusCode(), rawBody, exception);
        }
    }

    private static Map<String, List<String>> responseHeaders(Response response) {
        return response.getHeaders().asList().stream()
                .collect(Collectors.groupingBy(
                        header -> header.getName().toLowerCase(Locale.ROOT),
                        LinkedHashMap::new,
                        Collectors.mapping(Header::getValue, Collectors.toUnmodifiableList())));
    }
}
