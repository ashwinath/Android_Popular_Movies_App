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
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ashwin.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DetailFragment extends Fragment implements LoaderCallbacks<Cursor>,
        FetchReviewTask.ReviewAsyncResponse, FetchTrailerTask.TrailerAsyncResponse,
        DetailActivity.Clickable {
    private static final int DETAIL_LOADER = 0;
    FetchTrailerTask trailerAsyncTask;
    FetchReviewTask reviewAsyncTask;
    boolean hasReviewAsyncTasked = false;
    boolean hasTrailerAsyncTasked = false;
    boolean isFavourite;
    private Intent intent;
    private String movieId;
    private ContentValues thisMovieCV;

    // Views
    private ImageView backDropView;
    private ImageView posterView;
    private TextView titleView;
    private TextView dateView;
    private TextView ratingView;
    private TextView genresView;
    private TextView overviewView;
    private TextView reviewHeaderView;
    private NonScrollListView youtubeLinkListView;
    private NonScrollListView reviewListView;
    private TextView favouriteButtonView;

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

    private final String LOG_TAG = DetailFragment.class.getSimpleName();
    // review array stuff
    ArrayAdapter<String> mReviewAdapter;
    ArrayAdapter<String> mYoutubeAdapter;

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
        youtubeLinkListView = (NonScrollListView) rootView.findViewById(R.id.youtube_button_list);
        reviewListView = (NonScrollListView) rootView.findViewById(R.id.review_view_custom);
        reviewHeaderView = (TextView) rootView.findViewById(R.id.review_header);
        favouriteButtonView = (TextView) rootView.findViewById(R.id.favourite_text);

        return rootView;
    }

    /**
     * used to load the results from FetchReviewTask
     */
    @Override
    public void reviewProcessFinish(String[] output) {
        List<String> list = new ArrayList<>(Arrays.asList(output));
        if (!list.isEmpty()) {
            reviewHeaderView.setText("Reviews");
            mReviewAdapter = new ArrayAdapter<String>(getContext(), R.layout.review_text_view,
                    R.id.review_textview, list);
            reviewListView.setAdapter(mReviewAdapter);
        }
    }

    // onClick for favouriting
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void clickMethod(View v) {
        Log.v(LOG_TAG, "CLICKED");
        Cursor cursor = getContext().getContentResolver().query(
                MovieContract.FavouritesMoviesEntry.CONTENT_URI,
                new String[] { MovieContract.MovieColumns.COLUMN_MOVIE_ID },
                MovieContract.MovieColumns.COLUMN_MOVIE_ID + " = ?",
                new String[] { movieId },
                null,
                null
        );
        boolean isFavourited = (cursor.getCount() == 1);
        cursor.close();
        Log.v(LOG_TAG, "" + isFavourited);
        if (isFavourited) {
            getContext().getContentResolver().delete(
                    MovieContract.FavouritesMoviesEntry.CONTENT_URI,
                    MovieContract.MovieColumns.COLUMN_MOVIE_ID + " = ?",
                    new String[] { movieId }
            );
        } else {
            getContext().getContentResolver().insert(
                    MovieContract.FavouritesMoviesEntry.CONTENT_URI,
                    thisMovieCV
            );
        }
    }

    /**
     * used to load the results from FetchTrailerTask
     */
    @Override
    public void trailerProcessFinish(List<String> output) {
        String[] trailerNames = new String[output.size()];
        for (int i = 0; i < output.size(); ++i) {
            trailerNames[i] = "Trailer " + (i+1);
        }
        List<String> youtubeList = new ArrayList<>(Arrays.asList(trailerNames));
        mYoutubeAdapter = new ArrayAdapter<>(getContext(), R.layout.trailer_linear_layout,
                R.id.trailer_text, youtubeList);
        youtubeLinkListView.setAdapter(mYoutubeAdapter);
        final List<String> urls = Collections.unmodifiableList(output);

        youtubeLinkListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String url = urls.get(position);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        intent = getActivity().getIntent();
        isFavourite = intent.getExtras().getBoolean("isFavourite");
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

        // here we populate the content values of this movie
        thisMovieCV = new ContentValues();
        thisMovieCV.put(MovieContract.MovieColumns.COLUMN_MOVIE_ID, data.getString(COL_MOVIE_ID));
        thisMovieCV.put(MovieContract.MovieColumns.COLUMN_MOVIE_TITLE, data.getString(COL_MOVIE_TITLE));
        thisMovieCV.put(MovieContract.MovieColumns.COLUMN_MOVIE_OVERVIEW, data.getString(COL_MOVIE_OVERVIEW));
        thisMovieCV.put(MovieContract.MovieColumns.COLUMN_MOVIE_GENRES, data.getString(COL_MOVIE_GENRES));
        thisMovieCV.put(MovieContract.MovieColumns.COLUMN_MOVIE_POPULARITY, data.getString(COL_MOVIE_POPULARITY));
        thisMovieCV.put(MovieContract.MovieColumns.COLUMN_MOVIE_VOTE_COUNT, data.getString(COL_MOVIE_VOTE_COUNT));
        thisMovieCV.put(MovieContract.MovieColumns.COLUMN_MOVIE_VOTE_AVERAGE, data.getString(COL_MOVIE_VOTE_AVERAGE));
        thisMovieCV.put(MovieContract.MovieColumns.COLUMN_MOVIE_POSTER_PATH, data.getString(COL_MOVIE_POSTER_PATH));
        thisMovieCV.put(MovieContract.MovieColumns.COLUMN_MOVIE_BACKDROP_PATH, data.getString(COL_MOVIE_BACKDROP_PATH));
        thisMovieCV.put(MovieContract.MovieColumns.COLUMN_MOVIE_RELEASE_DATE, data.getString(COL_MOVIE_RELEASE_DATE));

        String backDropUrl = "http://image.tmdb.org/t/p/w342" + data.getString(COL_MOVIE_BACKDROP_PATH);
        String posterUrl = "http://image.tmdb.org/t/p/w185" + data.getString(COL_MOVIE_POSTER_PATH);
        String title = data.getString(COL_MOVIE_TITLE);
        String date = Utility.formatDate(data.getString(COL_MOVIE_RELEASE_DATE));
        String rating = Utility.formatRatings(data.getDouble(COL_MOVIE_VOTE_AVERAGE));
        String genres = Utility.formatGenres(data.getString(COL_MOVIE_GENRES));
        String overview = data.getString(COL_MOVIE_OVERVIEW);
        movieId = data.getString(COL_MOVIE_ID);

        // backdrop
        Picasso.with(getContext())
                .load(backDropUrl)
                .placeholder(R.raw.placeholder_backdrop)
                .into(backDropView);

        // poster
        Picasso.with(getContext())
                .load(posterUrl)
                .placeholder(R.raw.placeholder_backdrop)
                .into(posterView);

        // other texts
        titleView.setText(title);
        dateView.setText(date);
        ratingView.setText(rating);
        genresView.setText(genres);
        overviewView.setText(overview);

        // prevent it from executing it again once it has loaded
        if (!hasTrailerAsyncTasked) {
            trailerAsyncTask = new FetchTrailerTask(getContext());
            trailerAsyncTask.delegate = this;
            trailerAsyncTask.execute(movieId);
            hasTrailerAsyncTasked = true;
        }

        if (!hasReviewAsyncTasked) {
            reviewAsyncTask = new FetchReviewTask(getContext());
            // set delegate/listener back to this class
            reviewAsyncTask.delegate = this;

            // execute FetchReviewTask
            reviewAsyncTask.execute(movieId);

            // ensure that it does not load again
            hasReviewAsyncTasked = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        reviewAsyncTask.cancel(true);
        trailerAsyncTask.cancel(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }
}
