package com.example.ashwin.popularmovies.data;

import android.content.ContentProvider;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import com.example.ashwin.popularmovies.data.MovieContract.*;

public class MovieProvider extends ContentProvider{
    private static final String LOG_TAG = MovieProvider.class.getSimpleName();
    // the URI matcher used by this content provider
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mMovieHelper;

    static final int GENRES = 100;
    static final int MOVIE = 200;
    static final int MOVIE_ID = 201;
    static final int MOVIE_ID_GENRES = 202;

    static UriMatcher buildUriMatcher() {
        // initialise matcher with NO_MATCH (Common practice)
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String AUTHORITY = MovieContract.CONTENT_AUTHORITY;

        // use addURI to match the types
        matcher.addURI(AUTHORITY, MovieContract.PATH_GENRES, GENRES);
        matcher.addURI(AUTHORITY, MovieContract.PATH_MOVIES, MOVIE);
        matcher.addURI(AUTHORITY, MovieContract.PATH_MOVIES + "/#", MOVIE_ID);
        // not sure about this one
        matcher.addURI(AUTHORITY, MovieContract.PATH_MOVIES + "/#" + MovieContract.PATH_GENRES, MOVIE_ID_GENRES);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mMovieHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch(match) {
            case GENRES:
                return GenresEntry.CONTENT_TYPE;
            case MOVIE:
                return MoviesEntry.CONTENT_TYPE;
            case MOVIE_ID:
                return MoviesEntry.CONTENT_ITEM_TYPE;
            case MOVIE_ID_GENRES:
                return GenresEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        //TODO
    }
}
