package com.example.ashwin.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.Toast;

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

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

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
        gridview.setAdapter(new ImageAdapter(getActivity()));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(getActivity(), "" + position,
                        Toast.LENGTH_SHORT).show();
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
            Log.v("onOptionsItemSelected", "working");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mThumbIds.length;
        }

        public Object getItem(int position) {
            return null;
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

            Picasso.with(getContext())
                    .load(mThumbIds[position])
                    .placeholder(R.raw.placeholder)
                    .error(R.raw.big_problem)
                    .resize(396,594)
                    .centerCrop()
                    .into(imageView);

            return imageView;
        }

        // references to our images
        private Integer[] mThumbIds = {
                R.drawable.a1, R.drawable.a2,
                R.drawable.a3, R.drawable.a4,
                R.drawable.a5, R.drawable.a6,
                R.drawable.a7, R.drawable.a8,
                R.drawable.a9, R.drawable.a10,
                R.drawable.a11, R.drawable.a12,
        };
    }

    // insert async task here with parsing JSON
    public class FetchMovieTask extends AsyncTask<Void, Void, String[]> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
        protected String[] doInBackground(Void... params) {
            /*
            if (params.length == 0) {
                return null;
            }
            */
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
                        .appendQueryParameter("sort_by", "popularity.desc") // or vote_average.desc
                        .appendQueryParameter("api_key", "***REMOVED***");
                String website = builder.build().toString();
                URL url = new URL(website);
                Log.v(LOG_TAG, website);

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
                Log.v(LOG_TAG, movieJsonStr);
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

        private String[] getMovieDataFromJson (String movieJsonStr, int movieNum) throws JSONException {
            String[] resultStr = new String[movieNum*2];
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray resultArray = movieJson.getJSONArray("results");
            for (int i = 0, j = 0; i < resultArray.length(); ++i) {
                // even number = movie data string
                // odd number = poster path
                String originalTitle = resultArray.getJSONObject(i).getString("original_title");
                String overview = resultArray.getJSONObject(i).getString("overview");
                String userRating = resultArray.getJSONObject(i).getString("vote_average");
                String posterPath = resultArray.getJSONObject(i).getString("poster_path");
                resultStr[j++] = originalTitle + "\n" + userRating + "\n" + overview;
                resultStr[j++] = posterPath;
            }
            for (String str : resultStr) {
                Log.v(LOG_TAG, str);
            }
            return resultStr;
        }
    }
}
