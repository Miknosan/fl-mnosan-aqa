package io.testautomation.hygraph.features.moviecatalog;

import io.testautomation.hygraph.graphql.model.GraphQlResult;
import io.testautomation.hygraph.moviecatalog.client.MovieClient;
import io.testautomation.hygraph.moviecatalog.model.Movie;
import io.testautomation.hygraph.moviecatalog.model.MoviesData;
import io.testautomation.hygraph.moviecatalog.model.Publisher;

public final class MovieCatalogTestData {
    private final MovieClient client;
    private Movie movie;
    private Movie movieWithPublisher;

    public MovieCatalogTestData(MovieClient client) {
        this.client = client;
    }

    public synchronized Movie movie() {
        if (movie == null) {
            GraphQlResult<MoviesData> result = client.getCandidate();
            movie = requireCandidate(result, false);
        }
        return movie;
    }

    public synchronized Movie movieWithPublisher() {
        if (movieWithPublisher == null) {
            GraphQlResult<MoviesData> result = client.getPublisherCandidates();
            movieWithPublisher = requireCandidate(result, true);
        }
        return movieWithPublisher;
    }

    private static Movie requireCandidate(GraphQlResult<MoviesData> result, boolean publisherRequired) {
        if (result.statusCode() != 200 || result.body().hasErrors() || result.body().data() == null) {
            throw new IllegalStateException(
                    "Movie Catalog test-data precondition failed for operation " + result.operationName());
        }
        return result.body().data().movies().stream()
                .filter(MovieCatalogTestData::hasRequiredMovieFields)
                .filter(candidate -> !publisherRequired || hasCompletePublisher(candidate.publishedBy()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Movie Catalog dataset does not contain a suitable published movie"));
    }

    private static boolean hasRequiredMovieFields(Movie candidate) {
        return candidate.id() != null
                && !candidate.id().isBlank()
                && candidate.title() != null
                && !candidate.title().isBlank();
    }

    private static boolean hasCompletePublisher(Publisher publisher) {
        return publisher != null
                && publisher.id() != null
                && !publisher.id().isBlank()
                && publisher.name() != null
                && !publisher.name().isBlank();
    }
}
