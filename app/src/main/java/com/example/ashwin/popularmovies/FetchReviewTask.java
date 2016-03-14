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

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ashwin on 7/3/2016.
 */
public class FetchReviewTask extends AsyncTask<String, Void, String[]> {

    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
    private final Context mContext;
    public ReviewAsyncResponse delegate = null;

    public FetchReviewTask(Context context) {
        mContext = context;
    }

    protected String[] doInBackground(String... params) {
        if (params.length == 0) {
            return null;
        }
        // needs to be outside try catch to use in finally
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // will contain the raw JSON response as a string
        String movieJsonStr = null;
        try {
            //http://api.themoviedb.org/3/movie/{movie_id}/videos?api_key=**REMOVED**
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("movie")
                    .appendPath(params[0])
                    .appendPath("reviews")
                    .appendQueryParameter("api_key", mContext.getString(R.string.tmdb_api_key));
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
            return getReviewArray(movieJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    private String[] getReviewArray(String reviewJsonStr)
            throws JSONException {
        try {
            JSONObject reviewJson = new JSONObject(reviewJsonStr);
            JSONArray resultArray = reviewJson.getJSONArray("results");
            String[] reviewArray = new String[resultArray.length()];
            for (int i = 0; i < resultArray.length(); i++) {
                reviewArray[i] = resultArray.getJSONObject(i).getString("content");
            }
            return reviewArray;
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String[] result) {
        delegate.reviewProcessFinish(result);
    }

    public interface ReviewAsyncResponse {
        void reviewProcessFinish(String[] output);
    }
}
