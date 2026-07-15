package io.testautomation.hygraph.features.moviecatalog.scenarios;

import io.qase.commons.annotation.QaseId;
import io.testautomation.core.classification.Regression;
import io.testautomation.core.classification.Smoke;
import io.testautomation.hygraph.features.moviecatalog.MovieCatalogFeature;
import io.testautomation.hygraph.framework.metadata.Hygraph;
import io.testautomation.hygraph.framework.metadata.ReportGroup;
import io.testautomation.hygraph.graphql.model.GraphQlResult;
import io.testautomation.hygraph.moviecatalog.client.MovieClient;
import io.testautomation.hygraph.moviecatalog.model.Movie;
import io.testautomation.hygraph.moviecatalog.model.MoviesData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static io.testautomation.hygraph.features.moviecatalog.MovieAssertions.assertValidSummary;
import static io.testautomation.hygraph.framework.assertions.GraphQlAssertions.assertSuccessful;
import static org.assertj.core.api.Assertions.assertThat;

@Hygraph
@MovieCatalogFeature
@ReportGroup("List movies")
class ListMoviesTest {
    private static final int PAGE_SIZE = 2;

    @Test
    @Smoke
    @Regression
    @QaseId(38)
    @DisplayName("Verify that movie pagination returns distinct ordered pages when first and skip variables are provided")
    void shouldReturnDistinctOrderedMoviePagesWhenPaginationVariablesAreProvided(MovieClient client) {
        GraphQlResult<MoviesData> firstPage = client.getPage(PAGE_SIZE, 0);
        GraphQlResult<MoviesData> secondPage = client.getPage(PAGE_SIZE, PAGE_SIZE);

        assertSuccessful(firstPage);
        assertSuccessful(secondPage);

        List<Movie> firstMovies = firstPage.body().data().movies();
        List<Movie> secondMovies = secondPage.body().data().movies();
        assertThat(firstMovies).hasSize(PAGE_SIZE).allSatisfy(movie -> assertValidSummary(movie));
        assertThat(secondMovies).hasSize(PAGE_SIZE).allSatisfy(movie -> assertValidSummary(movie));

        List<String> firstIds = firstMovies.stream().map(Movie::id).toList();
        List<String> secondIds = secondMovies.stream().map(Movie::id).toList();
        List<String> allTitles = Stream.concat(firstMovies.stream(), secondMovies.stream())
                .map(Movie::title)
                .toList();

        assertThat(firstIds).doesNotHaveDuplicates().doesNotContainAnyElementsOf(secondIds);
        assertThat(secondIds).doesNotHaveDuplicates();
        assertThat(allTitles).isSorted();
    }
}
