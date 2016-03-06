package com.example.ashwin.popularmovies.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class MovieContract {

    public MovieContract() {
    }

    /**
     * Uri builder helper.
     */
    public static final String CONTENT_AUTHORITY = "com.example.ashwin.popularmovies.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MOVIES = "movies";

    /**
     * Movies interface for the Movies Database
     */
    public interface MovieColumns {
        String COLUMN_MOVIE_ID = "movie_id";
        String COLUMN_MOVIE_TITLE = "movie_title";
        String COLUMN_MOVIE_OVERVIEW = "movie_overview";
        String COLUMN_MOVIE_GENRES = "movie_genres";
        String COLUMN_MOVIE_POPULARITY = "movie_popularity";
        String COLUMN_MOVIE_VOTE_COUNT = "movie_vote_count";
        String COLUMN_MOVIE_VOTE_AVERAGE = "movie_vote_average";
        String COLUMN_MOVIE_POSTER_PATH = "movie_poster_path";
        String COLUMN_MOVIE_BACKDROP_PATH = "movie_backdrop_path";
        String COLUMN_MOVIE_FAVOURITED = "movie_favourited";
    }

    /**
     * Class that defines the Movies Table
     */
    public static final class MoviesEntry implements BaseColumns, MovieColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        // Table name
        public static final String TABLE_NAME = "movies";

        /**
         * Build Uri for movieId
         */
        public static Uri buildMovieUri(String movieId) {
            return CONTENT_URI.buildUpon().appendPath(movieId).build();
        }

        /**
         * read movie id from movie link uri
         */
        public static String getMovieId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
}
