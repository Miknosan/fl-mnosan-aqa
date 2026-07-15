package io.testautomation.hygraph.system.graphql.model;

import io.qase.commons.annotation.QaseIgnore;
import io.testautomation.core.classification.Regression;
import io.testautomation.core.classification.Smoke;
import io.testautomation.hygraph.framework.metadata.GraphQlPlatformFeature;
import io.testautomation.hygraph.framework.metadata.Hygraph;
import io.testautomation.hygraph.framework.metadata.ReportGroup;
import io.testautomation.hygraph.graphql.model.GraphQlError;
import io.testautomation.hygraph.graphql.model.GraphQlRequest;
import io.testautomation.hygraph.graphql.model.GraphQlResponse;
import io.testautomation.hygraph.graphql.model.GraphQlResult;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Hygraph
@GraphQlPlatformFeature
@ReportGroup("Protocol models")
class GraphQlModelContractTest {
    @Test
    @QaseIgnore
    @Smoke
    @Regression
    void shouldDefensivelyCopyRequestVariablesAndResponseCollections() {
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("first", 2);
        GraphQlRequest request = new GraphQlRequest("document", variables, "Movies");
        variables.put("first", 3);

        GraphQlError error = new GraphQlError("failure", null, null, null);
        GraphQlResponse<Object> response = new GraphQlResponse<>(null, List.of(error));

        assertThat(request.variables()).containsEntry("first", 2);
        assertThatThrownBy(() -> request.variables().put("skip", 1))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(response.errors()).containsExactly(error);
        assertThat(error.path()).isEmpty();
        assertThat(error.locations()).isEmpty();
        assertThat(error.extensions()).isEmpty();
    }

    @Test
    @QaseIgnore
    @Smoke
    @Regression
    void shouldRejectInvalidTransportResultState() {
        GraphQlResponse<Object> response = new GraphQlResponse<>(Map.of(), List.of());

        assertThatThrownBy(() -> new GraphQlResult<>(
                "Movies", 99, "application/json", Map.of(), Duration.ZERO, "{}", response))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("status code");
        assertThatThrownBy(() -> new GraphQlResult<>(
                " ", 200, "application/json", Map.of(), Duration.ZERO, "{}", response))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("operation name");
    }
}
