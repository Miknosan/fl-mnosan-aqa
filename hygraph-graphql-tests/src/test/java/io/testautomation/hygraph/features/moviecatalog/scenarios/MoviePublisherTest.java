package io.testautomation.hygraph.features.moviecatalog.scenarios;

import io.qase.commons.annotation.QaseId;
import io.testautomation.core.classification.Regression;
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

import static io.testautomation.hygraph.features.moviecatalog.MovieAssertions.assertPublisherMatches;
import static io.testautomation.hygraph.framework.assertions.GraphQlAssertions.assertSuccessful;
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
