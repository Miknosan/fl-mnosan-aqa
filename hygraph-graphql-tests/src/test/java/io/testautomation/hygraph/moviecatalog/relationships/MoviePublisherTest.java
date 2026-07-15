package io.testautomation.hygraph.moviecatalog.relationships;

import io.qase.commons.annotation.QaseId;
import io.testautomation.core.classification.Regression;
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
import static io.testautomation.hygraph.assertions.MovieAssertions.assertPublisherMatches;
import static org.assertj.core.api.Assertions.assertThat;

@Hygraph
@MovieCatalogFeature
@ReportGroup("Movie relationships")
class MoviePublisherTest {
    @Test
    @Regression
    @QaseId(40)
    @DisplayName("Verify that nested publisher data is returned through a fragment when a movie has publisher metadata")
    void shouldReturnPublisherThroughFragmentWhenMovieHasPublisherMetadata(
            MovieClient client,
            MovieCatalogTestData testData) {
        Movie candidate = testData.movieWithPublisher();

        GraphQlResult<MovieData> movieResult = client.getWithPublisher(candidate.id());

        assertSuccessful(movieResult);
        assertThat(movieResult.body().data()).isNotNull();
        Movie actual = movieResult.body().data().movie();
        assertPublisherMatches(actual, candidate);
    }
}
