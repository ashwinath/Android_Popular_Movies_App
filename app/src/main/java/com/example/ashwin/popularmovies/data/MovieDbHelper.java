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
    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "movie.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
                + MovieColumns.COLUMN_MOVIE_FAVOURITED + " INTEGER NOT NULL DEFAULT 0, "
                + "UNIQUE (" + MovieColumns.COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE)";

        db.execSQL(SQL_CREATE_MOVIES_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // do nothing as for now.
    }

    public static void deleteDatabase(Context context) {
        context.deleteDatabase(DATABASE_NAME);
    }
}
