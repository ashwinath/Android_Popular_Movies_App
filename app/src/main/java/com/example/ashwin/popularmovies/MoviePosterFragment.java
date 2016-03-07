package com.example.ashwin.popularmovies;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.ashwin.popularmovies.asynctasks.FetchMovieTask;
import com.example.ashwin.popularmovies.data.MovieContract;

public class MoviePosterFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private MovieAdapter mMovieAdapter;
    private static final int MOVIE_LOADER = 0;

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
            MovieContract.MovieColumns.COLUMN_MOVIE_FAVOURITED
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
    static final int COLUMN_MOVIE_FAVOURITED = 11;

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
                if (cursor != null) {
                    Intent intent = new Intent(getActivity(), DetailActivity.class)
                            .setData(MovieContract.MoviesEntry.buildMovieUri(
                                    cursor.getString(COL_MOVIE_ID)));
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
        FetchMovieTask movieTask = new FetchMovieTask(getContext());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortby = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_label_user_rating));
        movieTask.execute(sortby);
    }

    // KIV
    @Override
    public void onStart() {
        super.onStart();
        updateMovie();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = MovieContract.MoviesEntry._ID + " ASC";
        return new CursorLoader(getActivity(),
                MovieContract.MoviesEntry.CONTENT_URI,
                MOVIE_COLUMNS,
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

}
