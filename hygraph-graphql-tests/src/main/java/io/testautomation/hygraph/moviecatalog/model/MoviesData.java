package io.testautomation.hygraph.moviecatalog.model;

import java.util.List;

public record MoviesData(List<Movie> movies) {
    public MoviesData {
        movies = movies == null ? List.of() : List.copyOf(movies);
    }
}
