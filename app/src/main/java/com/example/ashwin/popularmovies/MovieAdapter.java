package com.example.ashwin.popularmovies;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.CursorAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class MovieAdapter extends CursorAdapter {

    private final String LOG_TAG = MovieAdapter.class.getSimpleName();

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public MovieAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    private String getPosterImageString(Cursor cursor) {
        String posterUrl = "http://image.tmdb.org/t/p/w185" + cursor.getString(MoviePosterFragment.COL_MOVIE_POSTER_PATH);
        Log.v(LOG_TAG, "Website: " + posterUrl);
        return posterUrl;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_view_movie, parent, false);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView imageView = (ImageView) view.findViewById(R.id.movie_poster_main);
        Picasso.with(context)
                .load(getPosterImageString(cursor))
                .placeholder(R.raw.placeholder)
                .into(imageView);
    }
}
