package com.example.ashwin.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
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
public class FetchTrailerTask extends AsyncTask<String, Void, String[]> {

    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
    private final Context mContext;

    public FetchTrailerTask (Context context) {
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
            //http://api.themoviedb.org/3/movie/{movie_id}/videos?api_key=***REMOVED***
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("movie")
                    .appendPath(params[0])
                    .appendPath("videos")
                    .appendQueryParameter("api_key", "***REMOVED***");
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
            return getTrailerArray(movieJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    private String[] getTrailerArray(String trailerJsonStr)
            throws JSONException {
        try {
            JSONObject trailerJson = new JSONObject(trailerJsonStr);
            JSONArray resultArray = trailerJson.getJSONArray("results");
            String[] trailerUrl = new String[resultArray.length()];
            for (int i = 0; i < resultArray.length(); i++) {
                trailerUrl[i] = "http://www.youtube.com/watch?v="
                        + resultArray.getJSONObject(i).getString("key");
            }
            return trailerUrl;
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }
}
