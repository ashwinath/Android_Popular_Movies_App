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

package com.example.ashwin.popularmovies;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.ashwin.popularmovies.data.MovieContract;

public class MoviePosterFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private MovieAdapter mMovieAdapter;
    private static final int MOVIE_LOADER = 0;
    private boolean isFavouriteStatus = false;
    private final String LOG_TAG = MoviePosterFragment.class.getSimpleName();

    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MoviesEntry.TABLE_NAME + "." + MovieContract.MoviesEntry._ID,
            MovieContract.MovieColumns.COLUMN_MOVIE_ID,
            MovieContract.MovieColumns.COLUMN_MOVIE_TITLE,
            MovieContract.MovieColumns.COLUMN_MOVIE_OVERVIEW,
            MovieContract.MovieColumns.COLUMN_MOVIE_GENRES,
            MovieContract.MovieColumns.COLUMN_MOVIE_POPULARITY,
            MovieContract.MovieColumns.COLUMN_MOVIE_VOTE_COUNT,
            MovieContract.MovieColumns.COLUMN_MOVIE_VOTE_AVERAGE,
            MovieContract.MovieColumns.COLUMN_MOVIE_POSTER_PATH,
            MovieContract.MovieColumns.COLUMN_MOVIE_BACKDROP_PATH,
            MovieContract.MovieColumns.COLUMN_MOVIE_RELEASE_DATE,
    };

    private static final String[] FAVOURITES_MOVIE_COLUMNS = {
            MovieContract.FavouritesMoviesEntry.FAVOURTIES_TABLE_NAME + "." + MovieContract.FavouritesMoviesEntry._ID,
            MovieContract.FavouritesMoviesEntry.COLUMN_MOVIE_ID,
            MovieContract.FavouritesMoviesEntry.COLUMN_MOVIE_TITLE,
            MovieContract.FavouritesMoviesEntry.COLUMN_MOVIE_OVERVIEW,
            MovieContract.FavouritesMoviesEntry.COLUMN_MOVIE_GENRES,
            MovieContract.FavouritesMoviesEntry.COLUMN_MOVIE_POPULARITY,
            MovieContract.FavouritesMoviesEntry.COLUMN_MOVIE_VOTE_COUNT,
            MovieContract.FavouritesMoviesEntry.COLUMN_MOVIE_VOTE_AVERAGE,
            MovieContract.FavouritesMoviesEntry.COLUMN_MOVIE_POSTER_PATH,
            MovieContract.FavouritesMoviesEntry.COLUMN_MOVIE_BACKDROP_PATH,
            MovieContract.FavouritesMoviesEntry.COLUMN_MOVIE_RELEASE_DATE,
    };

    // these indices are tied to the MOVIE_COLUMNS String
    static final int COL_MOVIE_TABLE_ID = 0;
    static final int COL_MOVIE_ID = 1;
    static final int COL_MOVIE_TITLE = 2;
    static final int COL_MOVIE_OVERVIEW = 3;
    static final int COL_MOVIE_GENRES = 4;
    static final int COL_MOVIE_POPULARITY = 5;
    static final int COL_MOVIE_VOTE_COUNT = 6;
    static final int COL_MOVIE_VOTE_AVERAGE = 7;
    static final int COL_MOVIE_POSTER_PATH = 8;
    static final int COL_MOVIE_BACKDROP_PATH = 9;
    static final int COL_MOVIE_RELEASE_DATE = 10;

    public MoviePosterFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMovieAdapter = new MovieAdapter(getActivity(), null, 0);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        GridView gridview = (GridView) rootView.findViewById(R.id.gridview);
        gridview.setAdapter(mMovieAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                Uri uri;
                if (isFavouriteStatus) {
                    uri = MovieContract.FavouritesMoviesEntry.buildMovieUri(
                            cursor.getString(COL_MOVIE_ID));
                } else {
                    uri = MovieContract.MoviesEntry.buildMovieUri(
                            cursor.getString(COL_MOVIE_ID));
                }

                if (uri != null) {
                    Intent intent = new Intent(getActivity(), DetailActivity.class)
                            .putExtra("isFavourite", isFavouriteStatus)
                            .setData(uri);
                    startActivity(intent);
                }
            }
        });
        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateMovie();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateMovie() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        isFavouriteStatus = isFavourite(prefs);
        if (!isFavouriteStatus) {
            FetchMovieTask movieTask = new FetchMovieTask(getContext());
            String sortby = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_label_user_rating));
            movieTask.execute(sortby);
        }
    }

    private boolean isFavourite(SharedPreferences prefs) {
        return getContext().getString(R.string.pref_sort_favourites).equals(
                prefs.getString(getString(R.string.pref_sort_key),
                        getString(R.string.pref_sort_label_user_rating)));
    }

    // KIV
    @Override
    public void onStart() {
        super.onStart();
        updateMovie();
    }

    @Override
    public void onResume() {
        super.onResume();
        onFavouritesChange();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder;
        Uri uri;
        String[] movieColumns;
        if (isFavouriteStatus) {
            sortOrder = MovieContract.FavouritesMoviesEntry._ID + " ASC";
            uri = MovieContract.FavouritesMoviesEntry.CONTENT_URI;
            movieColumns = FAVOURITES_MOVIE_COLUMNS;
        } else {
            sortOrder = MovieContract.MoviesEntry._ID + " ASC";
            uri = MovieContract.MoviesEntry.CONTENT_URI;
            movieColumns = MOVIE_COLUMNS;
        }

        return new CursorLoader(getActivity(),
                uri,
                movieColumns,
                null,
                null,
                sortOrder);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mMovieAdapter.swapCursor(cursor);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
    }

    void onFavouritesChange() {
        updateMovie();
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
    }
}
