package com.example.ashwin.popularmovies;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

public class MoviePosterFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
//    private ImageAdapter gridViewAdapter;

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
    static final int COLUMN_MOVIE_FAVOURITED = 10;

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
            FetchMovieTask movieTask = new FetchMovieTask();
            movieTask.execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateMovie() {
        FetchMovieTask movieTask = new FetchMovieTask();
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

    // insert async task here with parsing JSON
    public class FetchMovieTask extends AsyncTask<String, Void, String[][]> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
        protected String[][] doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }
            // needs to be outside try catch to use in finally
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // will contain the raw JSON response as a string
            String movieJsonStr = null;
            int movieNum = 20;
            try {
                // http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=***REMOVED***
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .authority("api.themoviedb.org")
                        .appendPath("3")
                        .appendPath("discover")
                        .appendPath("movie")
                        .appendQueryParameter("sort_by", params[0]) // or vote_average.desc
                        .appendQueryParameter("api_key", "***REMOVED***");
                if (params[0].equals(getString(R.string.pref_sort_user_rating))) {
                    builder.appendQueryParameter("primary_release_year", "2015");
                }
                String website = builder.build().toString();
                Log.v(LOG_TAG, website);
                URL url = new URL(website);

                // Create the request to themoviedb and open connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a string
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // nothing to do
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // doesn't affect JSON but it's a lot easier for a human to read
                    buffer.append(line +"\n");
                }

                if (buffer.length() == 0) {
                    // empty stream
                    return null;
                }
                movieJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error closing stream", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                getMovieDataFromJsonDb(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        private void getMovieDataFromJsonDb (String movieJsonStr)
                throws JSONException{
            try {
                String[] selectionArgs = {"nothing"};
                String selection = "nothing";
                getContext().getContentResolver().delete(MovieContract.BASE_CONTENT_URI, selection, selectionArgs);
                JSONObject movieJson = new JSONObject(movieJsonStr);
                JSONArray resultArray = movieJson.getJSONArray("results");
                // Store movie information into vectors first
                Vector<ContentValues> cVVector = new Vector<ContentValues>(resultArray.length());
                for (int i = 0; i < resultArray.length(); i++) {
                    JSONObject movieObject = resultArray.getJSONObject(i);
                    // data to be collected from json
                    String movieId = movieObject.getString("id");
                    String movieTitle = movieObject.getString("title");
                    String movieOverview = movieObject.getString("overview");
                    double moviePopularity = movieObject.getDouble("popularity");
                    int movieVoteCount = movieObject.getInt("vote_count");
                    double movieVoteAverage = movieObject.getDouble("vote_average");
                    String moviePosterPath = movieObject.getString("poster_path");
                    String movieBackdropPath = movieObject.getString("backdrop_path");
                    // genres might have a little problem. might want to comma seperate them
                    String movieGenres = "";
                    JSONArray genreArray = movieObject.getJSONArray("genre_ids");
//                    for (int j = 0; j < genreArray.length(); ++j) {
//                        movieGenres += genreArray.getJSONObject(i).toString();
//                        if (j < genreArray.length() - 1)
//                            movieGenres += ",";
//                    }

                    // add into content values vector
                    ContentValues contentValues = new ContentValues();

                    contentValues.put(MovieContract.MovieColumns.COLUMN_MOVIE_ID, movieId);
                    contentValues.put(MovieContract.MovieColumns.COLUMN_MOVIE_TITLE, movieTitle);
                    contentValues.put(MovieContract.MovieColumns.COLUMN_MOVIE_OVERVIEW, movieOverview);
                    contentValues.put(MovieContract.MovieColumns.COLUMN_MOVIE_GENRES, movieGenres);
                    contentValues.put(MovieContract.MovieColumns.COLUMN_MOVIE_POPULARITY, moviePopularity);
                    contentValues.put(MovieContract.MovieColumns.COLUMN_MOVIE_VOTE_COUNT, movieVoteCount);
                    contentValues.put(MovieContract.MovieColumns.COLUMN_MOVIE_VOTE_AVERAGE, movieVoteAverage);
                    contentValues.put(MovieContract.MovieColumns.COLUMN_MOVIE_POSTER_PATH, moviePosterPath);
                    contentValues.put(MovieContract.MovieColumns.COLUMN_MOVIE_BACKDROP_PATH, movieBackdropPath);

                    // now add into vector
                    cVVector.add(contentValues);
                }

                // BULK add everything into SQL database
                if (cVVector.size() > 0) {
                    ContentValues[] contentValues = cVVector.toArray(new ContentValues[cVVector.size()]);
                    Uri uri = MovieContract.MoviesEntry.CONTENT_URI;
                    getContext().getContentResolver().bulkInsert(uri, contentValues);
                }
                Log.d(LOG_TAG, "JSON parsing complete. " + cVVector.size() + " Inserted");
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

        }
    }
}
