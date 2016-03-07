package com.example.ashwin.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ashwin.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

public class DetailFragment extends Fragment implements LoaderCallbacks<Cursor> {
    private static final int DETAIL_LOADER = 0;

    // Views
    private ImageView backDropView;
    private ImageView posterView;
    private TextView titleView;
    private TextView dateView;
    private TextView ratingView;
    private TextView genresView;
    private TextView overviewView;

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

    private final String LOG_TAG = DetailFragment.class.getSimpleName();
    private String movieString;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        backDropView = (ImageView) rootView.findViewById(R.id.backdrop_image);
        posterView = (ImageView) rootView.findViewById(R.id.poster_image);
        titleView = (TextView) rootView.findViewById(R.id.title_text);
        dateView = (TextView) rootView.findViewById(R.id.date_text);
        ratingView = (TextView) rootView.findViewById(R.id.rating_text);
        genresView = (TextView) rootView.findViewById(R.id.genres_text);
        overviewView = (TextView) rootView.findViewById(R.id.overview_text);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Intent intent = getActivity().getIntent();
        if (intent == null)
            return null;
        return new CursorLoader(
                getActivity(),
                intent.getData(),
                MOVIE_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst())
            return;
        String backDropUrl = "http://image.tmdb.org/t/p/w342" + data.getString(COL_MOVIE_BACKDROP_PATH);
        String posterUrl = "http://image.tmdb.org/t/p/w342" + data.getString(COL_MOVIE_POSTER_PATH);
        String title = data.getString(COL_MOVIE_TITLE);
        String date = Utility.formatDate(data.getString(COL_MOVIE_RELEASE_DATE));
        String rating = Utility.formatRatings(data.getDouble(COL_MOVIE_VOTE_AVERAGE));
        String genres = Utility.formatGenres(data.getString(COL_MOVIE_GENRES));
        String overview = data.getString(COL_MOVIE_OVERVIEW);

        // backdrop
            Picasso.with(getContext())
                    .load(backDropUrl)
                    .placeholder(R.raw.placeholder)
                    .into(backDropView);

        // poster
        Picasso.with(getContext())
                .load(posterUrl)
                .placeholder(R.raw.placeholder)
                .into(posterView);

        // other texts
        titleView.setText(title);
        dateView.setText(date);
        ratingView.setText(rating);
        genresView.setText(genres);
        overviewView.setText(overview);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }
}
