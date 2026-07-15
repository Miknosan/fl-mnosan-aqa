package io.testautomation.hygraph.features.moviecatalog.scenarios;

import io.qase.commons.annotation.QaseId;
import io.testautomation.core.classification.Regression;
import io.testautomation.hygraph.features.moviecatalog.MovieCatalogFeature;
import io.testautomation.hygraph.framework.metadata.Hygraph;
import io.testautomation.hygraph.framework.metadata.ReportGroup;
import io.testautomation.hygraph.graphql.model.GraphQlError;
import io.testautomation.hygraph.graphql.model.GraphQlResult;
import io.testautomation.hygraph.moviecatalog.client.MovieClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.testautomation.hygraph.framework.assertions.GraphQlAssertions.assertRejected;
import static org.assertj.core.api.Assertions.assertThat;

@Hygraph
@MovieCatalogFeature
@ReportGroup("Query syntax validation")
class MovieQuerySyntaxValidationTest {
    @Test
    @Regression
    @QaseId(42)
    @DisplayName("Verify that a parse error is returned when the GraphQL query has an unclosed selection set")
    void shouldReturnParseErrorWhenSelectionSetIsNotClosed(MovieClient client) {
        GraphQlResult<Object> result = client.executeMalformedQuery();

        assertRejected(result);
        assertThat(result.body().errors())
                .extracting(GraphQlError::message)
                .anySatisfy(message -> assertThat(message)
                        .containsIgnoringCase("parse")
                        .contains("}"));
    }
}
