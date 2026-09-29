package com.cinemats.data;

import com.cinemats.model.Movie;

import java.util.ArrayList;
import java.util.List;

// Default movies catalogue mock data
public final class MovieMockData {

    private MovieMockData() {}

    // Returns default initial movies without price
    public static List<Movie> getInitialMovies() {
        List<Movie> movies = new ArrayList<>();
        movies.add(new Movie(1, "Interstellar", "Sci-Fi / Adventure", 169, "UA", "INTERSTELLAR", "NOW_SHOWING"));
        movies.add(new Movie(2, "Dune: Part Two", "Action / Adventure", 166, "UA", "DUNE: PART TWO", "NOW_SHOWING"));
        movies.add(new Movie(3, "Oppenheimer", "Biography / Drama", 180, "A", "OPPENHEIMER", "NOW_SHOWING"));
        movies.add(new Movie(4, "Spider-Man: Across The Spider-Verse", "Animation / Action", 140, "U", "SPIDER-MAN", "NOW_SHOWING"));
        movies.add(new Movie(5, "Inception", "Sci-Fi / Thriller", 148, "UA", "INCEPTION", "NOW_SHOWING"));
        movies.add(new Movie(6, "The Dark Knight", "Action / Crime", 152, "UA", "THE DARK KNIGHT", "NOW_SHOWING"));
        return movies;
    }
}
