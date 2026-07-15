package io.testautomation.hygraph.features.moviecatalog;

import io.testautomation.hygraph.moviecatalog.model.Movie;

import static org.assertj.core.api.Assertions.assertThat;

public final class MovieAssertions {
    private MovieAssertions() {
    }

    public static void assertValidSummary(Movie movie) {
        assertThat(movie.id()).isNotBlank();
        assertThat(movie.title()).isNotBlank();
        assertThat(movie.slug()).isNotBlank();
    }

    public static void assertMatches(Movie actual, Movie expected) {
        assertThat(actual)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    public static void assertPublisherMatches(Movie actual, Movie expected) {
        assertThat(actual).isNotNull();
        assertThat(actual.id()).isEqualTo(expected.id());
        assertThat(actual.title()).isEqualTo(expected.title());
        assertThat(actual.publishedBy()).usingRecursiveComparison().isEqualTo(expected.publishedBy());
    }
}
