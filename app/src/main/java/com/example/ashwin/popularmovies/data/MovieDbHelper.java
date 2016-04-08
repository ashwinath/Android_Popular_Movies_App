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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import com.example.ashwin.popularmovies.data.MovieContract.*;

/**
 * Manages local database for movie data
 */
public class MovieDbHelper extends SQLiteOpenHelper {
    // if you change the database schema, you must increment the database version
    private static final int DATABASE_VERSION = 2;
    static final String DATABASE_NAME = "movie.db";
    private Context context;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MoviesEntry.TABLE_NAME
                + "(" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + MovieColumns.COLUMN_MOVIE_ID + " TEXT NOT NULL, "
                + MovieColumns.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, "
                + MovieColumns.COLUMN_MOVIE_OVERVIEW + " TEXT, "
                + MovieColumns.COLUMN_MOVIE_GENRES + " TEXT, "
                + MovieColumns.COLUMN_MOVIE_POPULARITY + " REAL, "
                + MovieColumns.COLUMN_MOVIE_VOTE_COUNT + " INTEGER, "
                + MovieColumns.COLUMN_MOVIE_VOTE_AVERAGE + " REAL, "
                + MovieColumns.COLUMN_MOVIE_POSTER_PATH + " TEXT, "
                + MovieColumns.COLUMN_MOVIE_BACKDROP_PATH + " TEXT, "
                + MovieColumns.COLUMN_MOVIE_RELEASE_DATE + " TEXT, "
                + "UNIQUE (" + MovieColumns.COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE)";

        // here we create a table for favourites
        final String SQL_CREATE_FAVOURITES_TABLE = "CREATE TABLE " + FavouritesMoviesEntry.FAVOURTIES_TABLE_NAME
                + "(" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + FavouritesMoviesEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL, "
                + FavouritesMoviesEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, "
                + FavouritesMoviesEntry.COLUMN_MOVIE_OVERVIEW + " TEXT, "
                + FavouritesMoviesEntry.COLUMN_MOVIE_GENRES + " TEXT, "
                + FavouritesMoviesEntry.COLUMN_MOVIE_POPULARITY + " REAL, "
                + FavouritesMoviesEntry.COLUMN_MOVIE_VOTE_COUNT + " INTEGER, "
                + FavouritesMoviesEntry.COLUMN_MOVIE_VOTE_AVERAGE + " REAL, "
                + FavouritesMoviesEntry.COLUMN_MOVIE_POSTER_PATH + " TEXT, "
                + FavouritesMoviesEntry.COLUMN_MOVIE_BACKDROP_PATH + " TEXT, "
                + FavouritesMoviesEntry.COLUMN_MOVIE_RELEASE_DATE + " TEXT, "
                + "UNIQUE (" + FavouritesMoviesEntry.COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE)";
        db.execSQL(SQL_CREATE_MOVIES_TABLE);
        db.execSQL(SQL_CREATE_FAVOURITES_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        deleteDatabase(context);
    }

    public static void deleteDatabase(Context context) {
        context.deleteDatabase(DATABASE_NAME);
    }
}
