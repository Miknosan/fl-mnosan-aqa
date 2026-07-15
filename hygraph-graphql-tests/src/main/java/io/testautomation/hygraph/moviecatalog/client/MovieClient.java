package io.testautomation.hygraph.moviecatalog.client;

import com.fasterxml.jackson.core.type.TypeReference;
import io.testautomation.hygraph.graphql.client.GraphQlClient;
import io.testautomation.hygraph.graphql.document.GraphQlDocumentLoader;
import io.testautomation.hygraph.graphql.model.GraphQlRequest;
import io.testautomation.hygraph.graphql.model.GraphQlResponse;
import io.testautomation.hygraph.graphql.model.GraphQlResult;
import io.testautomation.hygraph.moviecatalog.model.MovieData;
import io.testautomation.hygraph.moviecatalog.model.MoviesData;

import java.util.Map;

public final class MovieClient {
    private static final String DOCUMENT_ROOT = "graphql/movie-catalog/";
    private static final TypeReference<GraphQlResponse<MoviesData>> MOVIES_RESPONSE = new TypeReference<>() {
    };
    private static final TypeReference<GraphQlResponse<MovieData>> MOVIE_RESPONSE = new TypeReference<>() {
    };
    private static final TypeReference<GraphQlResponse<Object>> ERROR_RESPONSE = new TypeReference<>() {
    };

    private final GraphQlClient graphQlClient;

    public MovieClient(GraphQlClient graphQlClient) {
        this.graphQlClient = graphQlClient;
    }

    public GraphQlResult<MoviesData> getPage(int first, int skip) {
        if (first < 1) {
            throw new IllegalArgumentException("Movie page size must be positive");
        }
        if (skip < 0) {
            throw new IllegalArgumentException("Movie page offset must not be negative");
        }
        return execute(
                "movies-page.graphql",
                Map.of("first", first, "skip", skip),
                "MoviesPage",
                MOVIES_RESPONSE);
    }

    public GraphQlResult<MoviesData> getCandidate() {
        return execute("movie-candidate.graphql", Map.of(), "MovieCandidate", MOVIES_RESPONSE);
    }

    public GraphQlResult<MoviesData> getPublisherCandidates() {
        return execute(
                "movie-publisher-candidates.graphql",
                Map.of(),
                "MoviePublisherCandidate",
                MOVIES_RESPONSE);
    }

    public GraphQlResult<MovieData> getById(String id) {
        return getById(id, "movie-by-id.graphql");
    }

    public GraphQlResult<MovieData> getSummaryById(String id) {
        return getById(id, "movie-summary-by-id.graphql");
    }

    private GraphQlResult<MovieData> getById(String id, String document) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Movie ID must not be blank");
        }
        return execute(document, Map.of("id", id), "MovieById", MOVIE_RESPONSE);
    }

    public GraphQlResult<MovieData> getWithPublisher(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Movie ID must not be blank");
        }
        return execute(
                "movie-with-publisher.graphql",
                Map.of("id", id),
                "MovieWithPublisher",
                MOVIE_RESPONSE);
    }

    public GraphQlResult<Object> executeMalformedQuery() {
        return execute(
                "validation/malformed-movie-query.txt",
                Map.of(),
                "MalformedMovieQuery",
                ERROR_RESPONSE);
    }

    public GraphQlResult<Object> executeUnknownFieldQuery() {
        return execute(
                "validation/movies-with-unknown-field.graphql",
                Map.of(),
                "MoviesWithUnknownField",
                ERROR_RESPONSE);
    }

    private <T> GraphQlResult<T> execute(
            String document,
            Map<String, Object> variables,
            String operationName,
            TypeReference<GraphQlResponse<T>> responseType) {
        GraphQlRequest request = new GraphQlRequest(
                GraphQlDocumentLoader.load(DOCUMENT_ROOT + document),
                variables,
                operationName);
        return graphQlClient.execute(request, responseType);
    }
}
