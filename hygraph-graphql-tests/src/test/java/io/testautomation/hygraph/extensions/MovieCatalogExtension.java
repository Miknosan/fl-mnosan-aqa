package io.testautomation.hygraph.extensions;

import io.testautomation.hygraph.moviecatalog.client.MovieClient;
import io.testautomation.hygraph.moviecatalog.data.MovieCatalogTestData;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public final class MovieCatalogExtension implements ParameterResolver {
    private static final String MOVIE_CLIENT = "movie-client";
    private static final String MOVIE_TEST_DATA = "movie-test-data";
    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(MovieCatalogExtension.class);

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> type = parameterContext.getParameter().getType();
        return type == MovieClient.class || type == MovieCatalogTestData.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext context)
            throws ParameterResolutionException {
        Class<?> type = parameterContext.getParameter().getType();
        if (type == MovieClient.class) {
            return movieClient(context);
        }
        if (type == MovieCatalogTestData.class) {
            return testData(context);
        }
        throw new ParameterResolutionException("Unsupported Movie Catalog parameter: " + type.getName());
    }

    private static MovieClient movieClient(ExtensionContext context) {
        return context.getRoot().getStore(NAMESPACE).getOrComputeIfAbsent(
                MOVIE_CLIENT,
                ignored -> new MovieClient(HygraphExtension.graphQlClient(context)),
                MovieClient.class);
    }

    private static MovieCatalogTestData testData(ExtensionContext context) {
        return context.getRoot().getStore(NAMESPACE).getOrComputeIfAbsent(
                MOVIE_TEST_DATA,
                ignored -> new MovieCatalogTestData(movieClient(context)),
                MovieCatalogTestData.class);
    }
}
