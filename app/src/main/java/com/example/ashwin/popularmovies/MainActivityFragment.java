package com.example.ashwin.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivityFragment extends Fragment {
    private ImageAdapter gridViewAdapter;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridview = (GridView) rootView.findViewById(R.id.gridview);
        gridViewAdapter = new ImageAdapter(getActivity());
        gridview.setAdapter(gridViewAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                // Put Poster into DetailActivity
                // TODO

                // Put words into DetailActivity
                String movieDetails = (gridViewAdapter.getMovieStr()).get(position);
                String ratingDetails = (gridViewAdapter.getRatingStr()).get(position);
                String synopsisDetails = (gridViewAdapter.getSynopsisStr()).get(position);
                String releaseDateDetails = (gridViewAdapter.getReleaseDateStr()).get(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, movieDetails + "\nRating: " + ratingDetails
                        + "\nRelease Date: " + releaseDateDetails + "\nSynopsis: " + synopsisDetails);
                startActivity(intent);
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

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<String> mThumbUris;
        private String drawablePrefix;
        private ArrayList<String> movieStr = new ArrayList<>();
        private ArrayList<String> ratingStr = new ArrayList<>();
        private ArrayList<String> synopsisStr = new ArrayList<>();
        private ArrayList<String> releaseDateStr = new ArrayList<>();

        private ArrayList<String> getReleaseDateStr() {
            return releaseDateStr;
        }

        private ArrayList<String> getMovieStr() {
            return movieStr;
        }

        private ArrayList<String> getRatingStr() {
            return ratingStr;
        }

        private ArrayList<String> getSynopsisStr() {
            return synopsisStr;
        }

        public ImageAdapter(Context c) {
            mContext = c;
            String packName = mContext.getPackageName();
            drawablePrefix = "android.resource://" + packName;

            ArrayList<String> uriPaths = new ArrayList<>();
            // not sure
            mThumbUris = uriPaths;
        }

        public int getCount() {
            return mThumbUris.size();
        }

        public Object getItem(int position) {
            return mThumbUris.get(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
            } else {
                imageView = (ImageView) convertView;
            }

            Uri imgUri = Uri.parse(mThumbUris.get(position));

            Picasso.with(getContext())
                    .load(imgUri) // just put website inside
                    .placeholder(R.raw.placeholder)
                    .into(imageView);
            imageView.setAdjustViewBounds(true);

            return imageView;
        }

        public ArrayList<String> getUriList() {
            return mThumbUris;
        }

    }

    public void updateMovie() {
        FetchMovieTask movieTask = new FetchMovieTask();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortby = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_user_rating));
        movieTask.execute(sortby);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovie();
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
                return getMovieDataFromJson (movieJsonStr, movieNum);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        // try using multi dimensional array instead.
        // one for poster image and the other for descriptions
        private String[][] getMovieDataFromJson (String movieJsonStr, int movieNum) throws JSONException {
            String[][] resultStr = new String[5][movieNum];
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray resultArray = movieJson.getJSONArray("results");
            for (int i = 0, j = 0; i < resultArray.length(); ++i) {
                // even number = movie data string
                // odd number = poster path
                String originalTitle = resultArray.getJSONObject(i).getString("original_title");
                String overview = resultArray.getJSONObject(i).getString("overview");
                String userRating = resultArray.getJSONObject(i).getString("vote_average");
                String posterPath = resultArray.getJSONObject(i).getString("poster_path");
                String releaseDate = resultArray.getJSONObject(i).getString("release_date");
                resultStr[1][i] = originalTitle;
                resultStr[2][i] = userRating;
                resultStr[3][i] = overview;
                resultStr[4][i] = releaseDate;
                resultStr[0][i] = posterPath;
            }
            return resultStr;
        }

        @Override
        protected void onPostExecute(String[][] result) {
            if (result != null) {
                ArrayList<String> uriPaths = gridViewAdapter.getUriList();
                uriPaths.clear();
                ArrayList<String> movieStr = gridViewAdapter.getMovieStr();
                ArrayList<String> ratingStr = gridViewAdapter.getRatingStr();
                ArrayList<String> synopsisStr = gridViewAdapter.getSynopsisStr();
                ArrayList<String> releaseDateStr = gridViewAdapter.getReleaseDateStr();
                for (int i = 0; i < result[1].length; ++i) {
                    String url = "http://image.tmdb.org/t/p/w342" + result[0][i];
                    uriPaths.add(url);
                    movieStr.add(result[1][i]);
                    ratingStr.add(result[2][i]);
                    synopsisStr.add(result[3][i]);
                    releaseDateStr.add(result[4][i]);
                }
                gridViewAdapter.notifyDataSetChanged();
            }
        }
    }
}
