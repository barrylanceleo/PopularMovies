package com.barrylanceleo.popularmovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ImageGridAdapter extends ArrayAdapter<Movie> {

    static final String TAG = ImageGridAdapter.class.getSimpleName();
    Picasso mPicasso;

    public ImageGridAdapter(Activity context) {
        super(context, 0, new ArrayList<Movie>());
        mPicasso = Picasso.with(context);
        //mPicasso.setIndicatorsEnabled(true);
    }

    private class ImageLoadedCallback implements Callback {
        ProgressBar mProgressBar;

        ImageLoadedCallback(ProgressBar mProgressBar) {
            this.mProgressBar = mProgressBar;
        }

        @Override
        public void onSuccess() {
            if (mProgressBar != null) {
                mProgressBar.setVisibility(View.GONE);
            }

        }

        @Override
        public void onError() {
            // NO-OP
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item_movie, parent, false);
        }

        ProgressBar mProgressBar = (ProgressBar) convertView.findViewById(R.id.movie_poster_grid__item_progress);
        mProgressBar.setVisibility(View.VISIBLE);
        ImageView posterView = (ImageView) convertView.findViewById(R.id.movie_poster_grid_item);

        Movie movie = getItem(position);
        mPicasso.load(movie.getPosterUrl())
                .into(posterView, new ImageLoadedCallback(mProgressBar));

        return convertView;

    }


}
