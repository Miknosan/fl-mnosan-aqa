package io.testautomation.hygraph.assertions;

import io.testautomation.hygraph.graphql.model.GraphQlError;
import io.testautomation.hygraph.graphql.model.GraphQlResult;

import static org.assertj.core.api.Assertions.assertThat;

public final class GraphQlAssertions {
    private GraphQlAssertions() {
    }

    public static void assertSuccessful(GraphQlResult<?> result) {
        assertJsonTransport(result, 200);
        assertThat(result.body().errors()).isEmpty();
    }

    public static void assertRejected(GraphQlResult<?> result) {
        assertJsonTransport(result, 400);
        assertThat(result.body().data()).isNull();
        assertThat(result.body().errors())
                .isNotEmpty()
                .extracting(GraphQlError::message)
                .allSatisfy(message -> assertThat(message).isNotBlank());
    }

    public static void assertJsonTransport(GraphQlResult<?> result, int expectedStatus) {
        assertThat(result.operationName()).isNotBlank();
        assertThat(result.statusCode()).isEqualTo(expectedStatus);
        assertThat(result.contentType()).startsWith("application/json");
        assertThat(result.elapsedTime()).isPositive();
        assertThat(result.rawBody()).isNotBlank();
    }
}
