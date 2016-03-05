package com.example.ashwin.popularmovies.data;

import android.content.ContentProvider;
import android.content.Context;
import android.content.UriMatcher;

public class MovieProvider extends ContentProvider{
    private static final String LOG_TAG = MovieProvider.class.getSimpleName();
    // the URI matcher used by this content provider
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mMovieHelper;

    static final int GENRES = 1;
    static final int MOVIE = 2;
    static final int MOVIE_ID = 21;
    static final int MOVIE_ID_GENRES = 22;

    static UriMatcher buildUriMatcher() {
        // initialise matcher with NO_MATCH (Common practice)
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        // use addURI to match the types
        matcher.addURI(authority, MovieContract.PATH_GENRES, GENRES);
        matcher.addURI(authority, MovieContract.PATH_MOVIES, MOVIE);
        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/*", MOVIE_ID);
        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/*" + MovieContract.PATH_GENRES, MOVIE_ID_GENRES);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mMovieHelper = new MovieDbHelper(getContext());
        return true;
    }

    private void deleteDatabase() {
        mMovieHelper.close();
        MovieDbHelper.deleteDatabase(getContext());
        mMovieHelper = new MovieDbHelper(getContext());
    }
}
