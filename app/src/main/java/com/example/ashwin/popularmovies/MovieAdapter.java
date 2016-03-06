package com.example.ashwin.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by ashwin on 7/3/2016.
 */
public class MovieAdapter extends CursorAdapter {

    public MovieAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    private String getPosterImageString(Cursor cursor) {
        String posterUrl = "http://image.tmdb.org/t/p/w185" + cursor.getString(MoviePosterFragment.COL_MOVIE_POSTER_PATH);
        return posterUrl;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_view_movie, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView imageView = new ImageView(context);
        Picasso.with(context)
                .load(getPosterImageString(cursor))
                .placeholder(R.raw.placeholder)
                .into(imageView);
        imageView.setAdjustViewBounds(true);
    }
}
