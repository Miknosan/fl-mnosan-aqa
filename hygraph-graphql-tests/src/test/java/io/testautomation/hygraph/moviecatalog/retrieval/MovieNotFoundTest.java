package io.testautomation.hygraph.moviecatalog.retrieval;

import io.qase.commons.annotation.QaseId;
import io.testautomation.core.classification.Regression;
import io.testautomation.hygraph.classification.Hygraph;
import io.testautomation.hygraph.classification.MovieCatalogFeature;
import io.testautomation.hygraph.graphql.model.GraphQlResult;
import io.testautomation.hygraph.moviecatalog.client.MovieClient;
import io.testautomation.hygraph.moviecatalog.model.MovieData;
import io.testautomation.hygraph.reporting.ReportGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.testautomation.hygraph.assertions.GraphQlAssertions.assertSuccessful;
import static org.assertj.core.api.Assertions.assertThat;

@Hygraph
@MovieCatalogFeature
@ReportGroup("Movie lookup validation")
class MovieNotFoundTest {
    private static final String NON_EXISTENT_MOVIE_ID = "nonexistentmovie000000000000";

    @Test
    @Regression
    @QaseId(41)
    @DisplayName("Verify that null movie data is returned without GraphQL errors when a non-existent movie ID is provided")
    void shouldReturnNullMovieWithoutErrorsWhenMovieIdDoesNotExist(MovieClient client) {
        GraphQlResult<MovieData> result = client.getSummaryById(NON_EXISTENT_MOVIE_ID);

        assertSuccessful(result);
        assertThat(result.body().data()).isNotNull();
        assertThat(result.body().data().movie()).isNull();
    }
}
