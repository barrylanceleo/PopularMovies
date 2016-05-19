package com.barrylanceleo.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.barrylanceleo.popularmovies.R;
import com.barrylanceleo.popularmovies.data.MovieContract;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Locale;

public class MovieGridAdapter extends CursorAdapter {

    //private static final String LOG_TAG = MovieGridAdapter.class.getSimpleName();
    private Picasso mPicasso;

    /**
     * Cache of the children views for a movie grid item.
     */
    private static class ViewHolder {
        private final TextView movieNameTextView;
        private final ImageView posterImageView;
        private final TextView yearTextView;
        private final TextView ratingTextView;

        private ViewHolder(View view) {
            movieNameTextView = (TextView) view.findViewById(R.id.grid_movie_name);
            posterImageView = (ImageView) view.findViewById(R.id.grid_poster);
            yearTextView = (TextView) view.findViewById(R.id.grid_release_year);
            ratingTextView = (TextView) view.findViewById(R.id.grid_rating);

        }
    }

    public MovieGridAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mPicasso = Picasso.with(context);
        //mPicasso.setIndicatorsEnabled(true);
    }

    private class ImageLoadedCallback implements Callback {
        TextView mMovieName;
        ImageView mPoster;
        ImageLoadedCallback(ImageView poster, TextView movieName) {
            this.mPoster = poster;
            this.mMovieName = movieName;
        }

        @Override
        public void onSuccess() {
            if (mMovieName != null) {
                mMovieName.setVisibility(View.GONE);
                mPoster.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }

        }

        @Override
        public void onError() {
            if (mMovieName != null) {
                mMovieName.setVisibility(View.GONE);
                mPoster.setScaleType(ImageView.ScaleType.FIT_START);
                mPoster.setImageResource(R.drawable.ic_error_outline_black_36dp);
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

        viewHolder.movieNameTextView.setText(cursor.getString(cursor.getColumnIndex
                (MovieContract.MovieDetailsEntry.COLUMN_TITLE)));
        viewHolder.movieNameTextView.setVisibility(View.VISIBLE);

        //Log.v(LOG_TAG, "Loading Cursor:" + DatabaseUtils.dumpCurrentRowToString(cursor));

        mPicasso.load(cursor.getString(cursor.getColumnIndex(MovieContract.MovieDetailsEntry.COLUMN_POSTER_URL)))
                .into(viewHolder.posterImageView,
                        new ImageLoadedCallback(viewHolder.posterImageView, viewHolder.movieNameTextView));

        viewHolder.yearTextView.setText(cursor.getString(cursor.getColumnIndex
                (MovieContract.MovieDetailsEntry.COLUMN_RELEASE_DATE)).substring(0, 4));

        viewHolder.ratingTextView.setText(String.format(Locale.US, "%.1f", cursor.getDouble(cursor.getColumnIndex
                (MovieContract.MovieDetailsEntry.COLUMN_VOTE_AVERAGE))));

    }

}
