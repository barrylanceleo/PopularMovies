package com.barrylanceleo.popularmovies;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.barrylanceleo.popularmovies.data.MovieContract;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class MovieDetailsFragment extends Fragment {

    static final String LOG_TAG = MovieGridAdapter.class.getSimpleName();

    private Context mContext;
    private View mRootView;
    private int mMovie_id;
    private ProgressBar mBackDropProgressBar;
    private ImageView mBackdropImageView;
    private ImageView mFavImageView;
    private TextView mTitleTextView;
    private TextView mOverviewTextView;
    private ProgressBar mPosterProgressBar;
    private ImageView mPosterImageView;
    private TextView mReleaseDateTextView;
    private TextView mRatingsTextView;
    private boolean isFavorite;

    public MovieDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMovie_id = getArguments().getInt("movie_id");

        // Inflate the layout for this fragment
        mRootView =  inflater.inflate(R.layout.fragment_movie_details, container, false);

        mBackDropProgressBar = (ProgressBar) mRootView.findViewById(R.id.backdrop_image_details_progress);
        mBackdropImageView = (ImageView) mRootView.findViewById(R.id.backdropImageViewDetails);
        mTitleTextView = (TextView) mRootView.findViewById(R.id.movie_title_details);
        mFavImageView = (ImageView) mRootView.findViewById(R.id.fav_image_details);
        mOverviewTextView = (TextView) mRootView.findViewById(R.id.movie_overview_details);
        mPosterProgressBar = (ProgressBar) mRootView.findViewById(R.id.poster_details_progress);
        mPosterImageView = (ImageView) mRootView.findViewById(R.id.poster_details_image_view);
        mReleaseDateTextView = (TextView) mRootView.findViewById(R.id.release_date_details);
        mRatingsTextView = (TextView) mRootView.findViewById(R.id.ratings_details);

        return mRootView;

    }


    /**
     * Called when the fragment's activity has been created and this
     * fragment's view hierarchy instantiated.  It can be used to do final
     * initialization once these pieces are in place, such as retrieving
     * views or restoring state.  It is also useful for fragments that use
     * {@link #setRetainInstance(boolean)} to retain their instance,
     * as this callback tells the fragment when it is fully associated with
     * the new activity instance.  This is called after {@link #onCreateView}
     * and before {@link #onViewStateRestored(Bundle)}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = getContext();
        mBackDropProgressBar.setVisibility(View.VISIBLE);
        mPosterProgressBar.setVisibility(View.VISIBLE);

        // query the details from the database
        Cursor movieCursor = mContext.getContentResolver().query(
                MovieContract.MovieDetailsEntry.buildMovieDetailsUri(mMovie_id),
                null,
                null,
                null,
                null);

        //Log.v(LOG_TAG, "Loading Cursor:" + DatabaseUtils.dumpCursorToString(movieCursor));
        if(movieCursor == null) {
            return;
        }

        movieCursor.moveToFirst();

        // load the backdrop image along with its progress bar
        Picasso.with(mContext).load(movieCursor.getString(movieCursor.getColumnIndex(
                    MovieContract.MovieDetailsEntry.COLUMN_BACKDROP_URL)))
                .into(mBackdropImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        mBackDropProgressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        // NO-OP
                    }
                });

        // set the movie title
        mTitleTextView.setText(movieCursor.getString(movieCursor.getColumnIndex(
                MovieContract.MovieDetailsEntry.COLUMN_TITLE)));

        // setup the fav button, check if its a favorite movie
        Cursor favCursor = mContext.getContentResolver().query(
                MovieContract.FavoriteMovieEntry.buildFavoriteMovieUri(mMovie_id),
                null,
                null,
                null,
                null);

        if (favCursor == null || favCursor.getCount() ==  0) {
            isFavorite = false;
            mFavImageView.setImageResource(R.drawable.ic_star_border_black_48dp);
        }
        else {
            isFavorite = true;
            mFavImageView.setImageResource(R.drawable.ic_star_black_48dp);
        }

        mFavImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFavorite) {
                    // removing from favorites
                    new AsyncTask<Integer, Void, Void>(){
                        @Override
                        protected Void doInBackground(Integer... params) {
                            getContext().getContentResolver().delete(
                                    MovieContract.FavoriteMovieEntry.CONTENT_URI,
                                    MovieContract.FavoriteMovieEntry.COLUMN_MOVIE_ID +" = ?",
                                    new String[]{Integer.toString(mMovie_id)}
                            );
                            return null;
                        }
                    }.execute();

                    mFavImageView.setImageResource(R.drawable.ic_star_border_black_48dp);
                    isFavorite = false;
                    Snackbar.make(mRootView.findViewById(R.id.fav_image_details),
                            "Removed from favorites",
                            Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                }
                else {
                    // add to favorites
                    new AsyncTask<Integer, Void, Void>(){
                        @Override
                        protected Void doInBackground(Integer... params) {
                            ContentValues favMovieCV = new ContentValues();
                            favMovieCV.put(MovieContract.FavoriteMovieEntry.COLUMN_MOVIE_ID, mMovie_id);
                            getContext().getContentResolver().insert(
                                    MovieContract.FavoriteMovieEntry.CONTENT_URI,
                                    favMovieCV
                            );
                            return null;
                        }
                    }.execute();
                    mFavImageView.setImageResource(R.drawable.ic_star_black_48dp);
                    isFavorite = true;
                    Snackbar.make(mRootView.findViewById(R.id.fav_image_details),
                            "Added to favorites",
                            Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                }

            }
        });

        // set the overview
        mOverviewTextView.setText("\t\t\t");
        mOverviewTextView.append(movieCursor.getString(movieCursor.getColumnIndex(
                MovieContract.MovieDetailsEntry.COLUMN_OVERVIEW)));

        // load poster image
        Picasso.with(mContext).load(movieCursor.getString(movieCursor.getColumnIndex(
                    MovieContract.MovieDetailsEntry.COLUMN_POSTER_URL)))
                .into(mPosterImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        mPosterProgressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        // NO-OP
                    }
                });

        // set the release date
        mReleaseDateTextView.append("\n" + movieCursor.getString(movieCursor.getColumnIndex(
                MovieContract.MovieDetailsEntry.COLUMN_RELEASE_DATE)));

        // set the ratings
        mRatingsTextView.append("\n" + movieCursor.getDouble(movieCursor.getColumnIndex(
                MovieContract.MovieDetailsEntry.COLUMN_VOTE_AVERAGE)) + "/10");

    }

}
