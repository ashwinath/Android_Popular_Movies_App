/*
 * Copyright 2016 Ashwin Nath Chatterji
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    public static final String CONTENT_AUTHORITY = "com.example.ashwin.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MOVIES = "movies";
    public static final String PATH_MOVIES_FAVOURITES = "movies_favourites";

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
        String COLUMN_MOVIE_RELEASE_DATE = "movie_release_date";
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

    public static final class FavouritesMoviesEntry implements BaseColumns, MovieColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES_FAVOURITES).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES_FAVOURITES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES_FAVOURITES;

        // Table name
        public static final String FAVOURTIES_TABLE_NAME = "movie_favourites";

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
