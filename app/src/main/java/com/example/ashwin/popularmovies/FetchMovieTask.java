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

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

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

public class FetchMovieTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
    private final Context mContext;

    public FetchMovieTask (Context context) {
        mContext = context;
    }

    protected Void doInBackground(String... params) {
        if (params.length == 0) {
            return null;
        }
        // needs to be outside try catch to use in finally
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // will contain the raw JSON response as a string
        String movieJsonStr = null;
        try {
            // http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=**REMOVED**
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("discover")
                    .appendPath("movie")
                    .appendQueryParameter("sort_by", params[0]) // or vote_average.desc
                    .appendQueryParameter("api_key", mContext.getString(R.string.tmdb_api_key));

            // here we need to get a minimum vote count, value is set to 1000 minimum votes
            if (params[0].equals(mContext.getString(R.string.pref_sort_user_rating)))
                builder.appendQueryParameter("vote_count.gte", "1000");

            String website = builder.build().toString();
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
            throws JSONException {
        try {
            String[] selectionArgs = {"nothing"};
            String selection = "nothing";
            mContext.getContentResolver().delete(MovieContract.BASE_CONTENT_URI, selection, selectionArgs);
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
                String movieGenres = movieObject.getString("genre_ids").replace("]", "").replace("[", "");
                String movieReleaseDate = movieObject.getString("release_date");

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
                contentValues.put(MovieContract.MovieColumns.COLUMN_MOVIE_RELEASE_DATE, movieReleaseDate);

                // now add into vector
                cVVector.add(contentValues);
            }

            int inserted = 0;
            // BULK add everything into SQL database
            if (cVVector.size() > 0) {
                ContentValues[] contentValues = cVVector.toArray(new ContentValues[cVVector.size()]);
                Uri uri = MovieContract.MoviesEntry.CONTENT_URI;
                inserted = mContext.getContentResolver().bulkInsert(uri, contentValues);
            }
            Log.d(LOG_TAG, "JSON parsing complete. " + inserted + " Inserted");
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

    }
}
