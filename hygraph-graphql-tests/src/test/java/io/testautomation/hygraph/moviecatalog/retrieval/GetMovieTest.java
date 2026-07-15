package io.testautomation.hygraph.moviecatalog.retrieval;

import io.qase.commons.annotation.QaseId;
import io.testautomation.core.classification.Regression;
import io.testautomation.core.classification.Smoke;
import io.testautomation.hygraph.classification.Hygraph;
import io.testautomation.hygraph.classification.MovieCatalogFeature;
import io.testautomation.hygraph.graphql.model.GraphQlResult;
import io.testautomation.hygraph.moviecatalog.client.MovieClient;
import io.testautomation.hygraph.moviecatalog.data.MovieCatalogTestData;
import io.testautomation.hygraph.moviecatalog.model.Movie;
import io.testautomation.hygraph.moviecatalog.model.MovieData;
import io.testautomation.hygraph.reporting.ReportGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.testautomation.hygraph.assertions.GraphQlAssertions.assertSuccessful;
import static io.testautomation.hygraph.assertions.MovieAssertions.assertMatches;
import static org.assertj.core.api.Assertions.assertThat;

@Hygraph
@MovieCatalogFeature
@ReportGroup("Retrieve movie")
class GetMovieTest {
    @Test
    @Smoke
    @Regression
    @QaseId(39)
    @DisplayName("Verify that a movie is returned by ID when a valid published movie identifier is provided")
    void shouldReturnMovieWhenPublishedMovieIdIsProvided(
            MovieClient client,
            MovieCatalogTestData testData) {
        Movie candidate = testData.movie();

        GraphQlResult<MovieData> movieResult = client.getById(candidate.id());

        assertSuccessful(movieResult);
        assertThat(movieResult.body().data()).isNotNull();
        assertMatches(movieResult.body().data().movie(), candidate);
    }
}
