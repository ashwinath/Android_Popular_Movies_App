package com.example.ashwin.popularmovies.data;

import android.content.ContentValues;
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
        final String SQL_CREATE_GENRES_TABLE = "CREATE TABLE " + GenresEntry.TABLE_NAME
                + "(" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + GenresColumns.COLUMN_GENRE_ID + "INTEGER NOT NULL, "
                + GenresColumns.COLUMN_GENRE_NAME + "TEXT NOT NULL, "
                + "UNIQUE (" + MovieContract.GenresColumns.COLUMN_GENRE_ID + ") ON CONFLICT REPLACE)";

        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MoviesEntry.TABLE_NAME
                + "(" + BaseColumns._ID + " INTEGER PRIMARY KEY, "
                + MovieColumns.COLUMN_MOVIE_ID + " INTEGER NOT NULL, "
                + MovieColumns.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, "
                + MovieColumns.COLUMN_MOVIE_OVERVIEW + "TEXT, "
                + MovieColumns.COLUMN_MOVIE_POPULARITY + "REAL, "
                + MovieColumns.COLUMN_MOVIE_VOTE_COUNT + "INTEGER, "
                + MovieColumns.COLUMN_MOVIE_VOTE_AVERAGE + "REAL, "
                + MovieColumns.COLUMN_MOVIE_POSTER_PATH + "TEXT, "
                + MovieColumns.COLUMN_MOVIE_BACKDROP_PATH + "TEXT, "
                + MovieColumns.COLUMN_MOVIE_FAVOURITED + "INTEGER NOT NULL DEFAULT 0, "
                + "UNIQUE (" + MovieColumns.COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE)";

        // not sure if im going to use this table
        final String SQL_CREATE_MOVIE_GENRES_TABLE = "CREATE TABLE " + MoviesEntry.TABLE_NAME_MOVIE_GENRES
                + "(" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MovieColumns.COLUMN_MOVIE_ID + " TEXT NOT NULL REFERENCES " + MoviesEntry.TABLE_NAME +
                "(" + MovieColumns.COLUMN_MOVIE_ID + "), "
                + GenresColumns.COLUMN_GENRE_ID + " INTEGER NOT NULL REFERENCES " + GenresEntry.TABLE_NAME +
                "(" + GenresColumns.COLUMN_GENRE_ID + "), "
                + "UNIQUE (" + MovieColumns.COLUMN_MOVIE_ID + ", " + GenresColumns.COLUMN_GENRE_ID
                + ") ON CONFLICT REPLACE)";

        db.execSQL(SQL_CREATE_GENRES_TABLE);
        db.execSQL(SQL_CREATE_MOVIES_TABLE);
        db.execSQL(SQL_CREATE_MOVIE_GENRES_TABLE);

        /**
         * Manually fill up what each genre id means
         * From http://docs.themoviedb.apiary.io/#reference/genres/genremovielist/get
         */
        db.insert(GenresEntry.TABLE_NAME, null, rowContents(28, "Action"));
        db.insert(GenresEntry.TABLE_NAME, null, rowContents(12, "Adventure"));
        db.insert(GenresEntry.TABLE_NAME, null, rowContents(16, "Animation"));
        db.insert(GenresEntry.TABLE_NAME, null, rowContents(35, "Comedy"));
        db.insert(GenresEntry.TABLE_NAME, null, rowContents(80, "Crime"));
        db.insert(GenresEntry.TABLE_NAME, null, rowContents(99, "Documentary"));
        db.insert(GenresEntry.TABLE_NAME, null, rowContents(18, "Drama"));
        db.insert(GenresEntry.TABLE_NAME, null, rowContents(10751, "Family"));
        db.insert(GenresEntry.TABLE_NAME, null, rowContents(14, "Fantasy"));
        db.insert(GenresEntry.TABLE_NAME, null, rowContents(10769, "Foreign"));
        db.insert(GenresEntry.TABLE_NAME, null, rowContents(36, "History"));
        db.insert(GenresEntry.TABLE_NAME, null, rowContents(27, "Horror"));
        db.insert(GenresEntry.TABLE_NAME, null, rowContents(10402, "Music"));
        db.insert(GenresEntry.TABLE_NAME, null, rowContents(9648, "Mystery"));
        db.insert(GenresEntry.TABLE_NAME, null, rowContents(10749, "Romance"));
        db.insert(GenresEntry.TABLE_NAME, null, rowContents(878, "Science Fiction"));
        db.insert(GenresEntry.TABLE_NAME, null, rowContents(10770, "TV Movie"));
        db.insert(GenresEntry.TABLE_NAME, null, rowContents(53, "Thriller"));
        db.insert(GenresEntry.TABLE_NAME, null, rowContents(10752, "War"));
        db.insert(GenresEntry.TABLE_NAME, null, rowContents(37, "Western"));

    }

    /**
     * Helper method to insert genres into genres table
     */
    private static ContentValues rowContents(int id, String name) {
        ContentValues rowValues = new ContentValues();
        rowValues.put(GenresColumns.COLUMN_GENRE_ID, id);
        rowValues.put(GenresColumns.COLUMN_GENRE_NAME, name);
        return rowValues;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // do nothing as for now.
    }

    public static void deleteDatabase(Context context) {
        context.deleteDatabase(DATABASE_NAME);
    }
}
