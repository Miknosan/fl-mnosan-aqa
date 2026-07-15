package io.testautomation.hygraph.moviecatalog.validation;

import io.qase.commons.annotation.QaseId;
import io.testautomation.core.classification.Regression;
import io.testautomation.hygraph.classification.Hygraph;
import io.testautomation.hygraph.classification.MovieCatalogFeature;
import io.testautomation.hygraph.graphql.model.GraphQlError;
import io.testautomation.hygraph.graphql.model.GraphQlResult;
import io.testautomation.hygraph.moviecatalog.client.MovieClient;
import io.testautomation.hygraph.reporting.ReportGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.testautomation.hygraph.assertions.GraphQlAssertions.assertRejected;
import static org.assertj.core.api.Assertions.assertThat;

@Hygraph
@MovieCatalogFeature
@ReportGroup("Schema validation")
class MovieSchemaValidationTest {
    @Test
    @Regression
    @QaseId(43)
    @DisplayName("Verify that a validation error is returned when a non-existent Movie field is requested")
    void shouldReturnValidationErrorWhenMovieFieldDoesNotExist(MovieClient client) {
        GraphQlResult<Object> result = client.executeUnknownFieldQuery();

        assertRejected(result);
        assertThat(result.body().errors())
                .extracting(GraphQlError::message)
                .anySatisfy(message -> assertThat(message)
                        .contains("nonExistentField")
                        .containsIgnoringCase("Movie"));
    }
}
