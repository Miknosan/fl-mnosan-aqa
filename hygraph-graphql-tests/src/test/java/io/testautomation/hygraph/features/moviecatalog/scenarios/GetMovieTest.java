package io.testautomation.hygraph.features.moviecatalog.scenarios;

import io.qase.commons.annotation.QaseId;
import io.testautomation.core.classification.Regression;
import io.testautomation.core.classification.Smoke;
import io.testautomation.hygraph.features.moviecatalog.MovieCatalogFeature;
import io.testautomation.hygraph.features.moviecatalog.MovieCatalogTestData;
import io.testautomation.hygraph.framework.metadata.Hygraph;
import io.testautomation.hygraph.framework.metadata.ReportGroup;
import io.testautomation.hygraph.graphql.model.GraphQlResult;
import io.testautomation.hygraph.moviecatalog.client.MovieClient;
import io.testautomation.hygraph.moviecatalog.model.Movie;
import io.testautomation.hygraph.moviecatalog.model.MovieData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.testautomation.hygraph.features.moviecatalog.MovieAssertions.assertMatches;
import static io.testautomation.hygraph.framework.assertions.GraphQlAssertions.assertSuccessful;
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
