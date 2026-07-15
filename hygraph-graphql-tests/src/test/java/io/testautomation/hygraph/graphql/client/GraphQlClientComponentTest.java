package io.testautomation.hygraph.graphql.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.qase.commons.annotation.QaseIgnore;
import io.testautomation.core.classification.Regression;
import io.testautomation.core.classification.Smoke;
import io.testautomation.hygraph.classification.GraphQlPlatformFeature;
import io.testautomation.hygraph.classification.Hygraph;
import io.testautomation.hygraph.config.HygraphConfig;
import io.testautomation.hygraph.graphql.document.GraphQlDocumentLoader;
import io.testautomation.hygraph.graphql.model.GraphQlRequest;
import io.testautomation.hygraph.graphql.model.GraphQlResponse;
import io.testautomation.hygraph.graphql.model.GraphQlResult;
import io.testautomation.hygraph.reporting.ReportGroup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Hygraph
@GraphQlPlatformFeature
@ReportGroup("GraphQL transport")
class GraphQlClientComponentTest {
    private static final TypeReference<GraphQlResponse<Map<String, String>>> RESPONSE_TYPE = new TypeReference<>() {
    };
    private final AtomicReference<String> requestBody = new AtomicReference<>();
    private HttpServer server;
    private GraphQlClient client;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.start();
        URI endpoint = URI.create("http://localhost:" + server.getAddress().getPort());
        client = new GraphQlClient(new HygraphConfig(endpoint, Duration.ofSeconds(2)));
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    @QaseIgnore
    @Smoke
    @Regression
    void shouldPreserveTransportMetadataWhenResponseIsValid() {
        respondWith(200, "{\"data\":{\"value\":\"ok\"}}", "trace-123");

        GraphQlResult<Map<String, String>> result = client.execute(
                request("health.graphql", Map.of("limit", 1), "Health"),
                RESPONSE_TYPE);

        assertThat(result.operationName()).isEqualTo("Health");
        assertThat(result.statusCode()).isEqualTo(200);
        assertThat(result.contentType()).startsWith("application/json");
        assertThat(result.headers()).containsEntry("x-trace-id", java.util.List.of("trace-123"));
        assertThat(result.elapsedTime()).isPositive();
        assertThat(result.rawBody()).contains("\"value\":\"ok\"");
        assertThat(result.body().data()).containsEntry("value", "ok");
        assertThat(requestBody.get())
                .contains("\"operationName\":\"Health\"")
                .contains("\"variables\":{\"limit\":1}");
    }

    @Test
    @QaseIgnore
    @Smoke
    @Regression
    void shouldPreservePartialDataAndErrorsWhenResponseContainsBoth() {
        respondWith(
                200,
                "{\"data\":{\"value\":\"partial\"},\"errors\":[{\"message\":\"degraded\","
                        + "\"path\":[\"value\"],\"locations\":[{\"line\":1,\"column\":2}],"
                        + "\"extensions\":{\"code\":\"WARN\"}}]}",
                "trace-456");

        GraphQlResult<Map<String, String>> result = client.execute(
                request("partial.graphql", Map.of(), "Partial"),
                RESPONSE_TYPE);

        assertThat(result.body().data()).containsEntry("value", "partial");
        assertThat(result.body().errors()).singleElement().satisfies(error -> {
            assertThat(error.message()).isEqualTo("degraded");
            assertThat(error.path()).hasSize(1);
            assertThat(error.locations()).hasSize(1);
            assertThat(error.extensions()).containsKey("code");
        });
    }

    @Test
    @QaseIgnore
    @Smoke
    @Regression
    void shouldExposeOperationAndResponseWhenDeserializationFails() {
        respondWith(502, "not-json", "trace-789");
        GraphQlRequest request = request("broken.graphql", Map.of(), "Broken");

        assertThatThrownBy(() -> client.execute(request, RESPONSE_TYPE))
                .isInstanceOf(GraphQlResponseDeserializationException.class)
                .satisfies(exception -> {
                    GraphQlResponseDeserializationException failure =
                            (GraphQlResponseDeserializationException) exception;
                    assertThat(failure.operationName()).isEqualTo("Broken");
                    assertThat(failure.statusCode()).isEqualTo(502);
                    assertThat(failure.responseBody()).isEqualTo("not-json");
                    assertThat(failure).hasMessageContaining("Broken").hasMessageContaining("502");
                });
    }

    private void respondWith(int statusCode, String body, String traceId) {
        server.createContext("/", exchange -> sendResponse(exchange, statusCode, body, traceId));
    }

    private static GraphQlRequest request(String document, Map<String, Object> variables, String operationName) {
        return new GraphQlRequest(
                GraphQlDocumentLoader.load("graphql/component/" + document),
                variables,
                operationName);
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String body, String traceId) throws IOException {
        requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        byte[] response = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.getResponseHeaders().add("X-Trace-Id", traceId);
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}
