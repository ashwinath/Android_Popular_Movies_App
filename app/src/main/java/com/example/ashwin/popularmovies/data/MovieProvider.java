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

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.ashwin.popularmovies.data.MovieContract.*;


public final class MovieProvider extends ContentProvider {
    private static final String LOG_TAG = MovieProvider.class.getSimpleName();
    // the URI matcher used by this content provider
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mMovieHelper;

    static final int MOVIE = 200;
    static final int MOVIE_ID = 201;
    static final int MOVIE_FAVOURITES = 301;
    static final int MOVIE_FAVOURITES_ID = 302;

    static UriMatcher buildUriMatcher() {
        // initialise matcher with NO_MATCH (Common practice)
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String AUTHORITY = MovieContract.CONTENT_AUTHORITY;

        matcher.addURI(AUTHORITY, MovieContract.PATH_MOVIES, MOVIE);
        matcher.addURI(AUTHORITY, MovieContract.PATH_MOVIES + "/*", MOVIE_ID);
        matcher.addURI(AUTHORITY, MovieContract.PATH_MOVIES_FAVOURITES, MOVIE_FAVOURITES);
        matcher.addURI(AUTHORITY, MovieContract.PATH_MOVIES_FAVOURITES + "/*", MOVIE_FAVOURITES_ID);

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
            case MOVIE:
                return MoviesEntry.CONTENT_TYPE;
            case MOVIE_ID:
                return MoviesEntry.CONTENT_ITEM_TYPE;
            case MOVIE_FAVOURITES:
                return FavouritesMoviesEntry.CONTENT_TYPE;
            case MOVIE_FAVOURITES_ID:
                return FavouritesMoviesEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
    }

    // movies.movie_id = ?
    private static final String sMovieIdSettingSelection =
            MoviesEntry.TABLE_NAME + "." + MovieColumns.COLUMN_MOVIE_ID + " = ?";

    private Cursor getMovieByMovieId(Uri uri, String[] projection, String sortOrder) {
        String movieIdString = MoviesEntry.getMovieId(uri);

        String[] selectionArgs = new String[] {movieIdString};
        String selection = sMovieIdSettingSelection;
        return mMovieHelper.getReadableDatabase().query(
                MoviesEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private static final String sMovieFavouriteIdSettingSelection =
            FavouritesMoviesEntry.FAVOURTIES_TABLE_NAME + "." + MovieColumns.COLUMN_MOVIE_ID + " = ?";

    private Cursor getFavouriteMovieByMovieId(Uri uri, String[] projection, String sortOrder) {
        String movieIdString = MoviesEntry.getMovieId(uri);

        String[] selectionArgs = new String[] {movieIdString};
        String selection = sMovieFavouriteIdSettingSelection;
        return mMovieHelper.getReadableDatabase().query(
                FavouritesMoviesEntry.FAVOURTIES_TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case MOVIE:
                retCursor = mMovieHelper.getReadableDatabase().query(
                        MoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case MOVIE_ID:
                retCursor = getMovieByMovieId(uri, projection, sortOrder);
                break;

            case MOVIE_FAVOURITES:
                retCursor = mMovieHelper.getReadableDatabase().query(
                        FavouritesMoviesEntry.FAVOURTIES_TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case MOVIE_FAVOURITES_ID:
                retCursor = getFavouriteMovieByMovieId(uri, projection, sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mMovieHelper.getWritableDatabase();
        Uri returnUri;
        if (sUriMatcher.match(uri) == MOVIE) {
            long _id = db.insert(MoviesEntry.TABLE_NAME, null, values);
            if (_id > 0)
                returnUri = MoviesEntry.buildMovieUri(values.getAsString(MoviesEntry.COLUMN_MOVIE_ID));
            else
                throw new android.database.SQLException("Failed to insert row into " + uri);

        } else if (sUriMatcher.match(uri) == MOVIE_FAVOURITES) {
            long _id = db.insert(FavouritesMoviesEntry.FAVOURTIES_TABLE_NAME, null, values);
            if (_id > 0)
                returnUri = FavouritesMoviesEntry.buildMovieUri(values.getAsString(MoviesEntry.COLUMN_MOVIE_ID));
            else
                throw new android.database.SQLException("Failed to insert row into " + uri);
        } else {
            throw new UnsupportedOperationException("Unknown uri " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (uri.equals(MovieContract.BASE_CONTENT_URI)) {
            final SQLiteDatabase db = mMovieHelper.getWritableDatabase();
            db.delete(MoviesEntry.TABLE_NAME, null, null);
            getContext().getContentResolver().notifyChange(uri, null);
            return 1;
        } else if (sUriMatcher.match(uri) == MOVIE) {
            final SQLiteDatabase db = mMovieHelper.getWritableDatabase();
            int rowsDeleted;
            if (selection == null)
                selection = "1";
            rowsDeleted = db.delete(MoviesEntry.TABLE_NAME, selection, selectionArgs);
            if (rowsDeleted != 0)
                getContext().getContentResolver().notifyChange(uri, null);
            return rowsDeleted;
        } else if (sUriMatcher.match(uri) == MOVIE_FAVOURITES) {
            final SQLiteDatabase db = mMovieHelper.getWritableDatabase();
            int rowsDeleted;
            if (selection == null)
                selection = "1";
            rowsDeleted = db.delete(FavouritesMoviesEntry.FAVOURTIES_TABLE_NAME, selection, selectionArgs);
            if (rowsDeleted != 0)
                getContext().getContentResolver().notifyChange(uri, null);
            return rowsDeleted;
        } else {
            throw new UnsupportedOperationException("Unknown uri " + uri);
        }

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mMovieHelper.getWritableDatabase();
        int rowsUpdated;
        if (sUriMatcher.match(uri) == MOVIE) {
            rowsUpdated = db.update(MoviesEntry.TABLE_NAME, values, selection, selectionArgs);
            if (rowsUpdated != 0)
                getContext().getContentResolver().notifyChange(uri, null);
            return rowsUpdated;
        } else if (sUriMatcher.match(uri) == MOVIE_FAVOURITES) {
            rowsUpdated = db.update(FavouritesMoviesEntry.FAVOURTIES_TABLE_NAME, values, selection, selectionArgs);
            if (rowsUpdated != 0)
                getContext().getContentResolver().notifyChange(uri, null);
            return rowsUpdated;
        } else {
            throw new UnsupportedOperationException("Unknown uri " + uri);
        }
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mMovieHelper.getWritableDatabase();
        if (sUriMatcher.match(uri) == MOVIE) {
            db.beginTransaction();
            int returnCount = 0;
            try {
                for (ContentValues value : values) {
                    long _id = db.insert(MoviesEntry.TABLE_NAME, null, value);
                    if (_id != -1)
                        ++returnCount;
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            getContext().getContentResolver().notifyChange(uri, null);
            return returnCount;
        } else {
            return super.bulkInsert(uri, values);
        }
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mMovieHelper.close();
        super.shutdown();
    }
}
