package com.barrylanceleo.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.barrylanceleo.popularmovies.R;
import com.barrylanceleo.popularmovies.data.MovieContract;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class MovieGridAdapter extends CursorAdapter {

    //private static final String LOG_TAG = MovieGridAdapter.class.getSimpleName();
    Picasso mPicasso;

    /**
     * Cache of the children views for a movie grid item.
     */
    public static class ViewHolder {
        public final ProgressBar progressBar;
        public final ImageView posterView;

        public ViewHolder(View view) {
            progressBar = (ProgressBar) view.findViewById(R.id.movie_poster_grid__item_progress);
            posterView = (ImageView) view.findViewById(R.id.movie_poster_grid_item);
        }
    }

    public MovieGridAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mPicasso = Picasso.with(context);
        //mPicasso.setIndicatorsEnabled(true);
    }

    private class ImageLoadedCallback implements Callback {
        ProgressBar mProgressBar;
        ImageView mPosterView;
        ImageLoadedCallback(ImageView mPosterView, ProgressBar mProgressBar) {
            this.mPosterView = mPosterView;
            this.mProgressBar = mProgressBar;
        }

        @Override
        public void onSuccess() {
            if (mProgressBar != null) {
                mProgressBar.setVisibility(View.GONE);
                mPosterView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }

        }

        @Override
        public void onError() {
            if (mProgressBar != null) {
                mProgressBar.setVisibility(View.GONE);
                mPosterView.setScaleType(ImageView.ScaleType.CENTER);
                mPosterView.setImageResource(R.drawable.ic_error_outline_black_36dp);
            }
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_item_movie, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.progressBar.setVisibility(View.VISIBLE);

        //Log.v(LOG_TAG, "Loading Cursor:" + DatabaseUtils.dumpCurrentRowToString(cursor));

        mPicasso.load(cursor.getString(cursor.getColumnIndex(MovieContract.MovieDetailsEntry.COLUMN_POSTER_URL)))
                .into(viewHolder.posterView,
                        new ImageLoadedCallback(viewHolder.posterView, viewHolder.progressBar));

    }

}
