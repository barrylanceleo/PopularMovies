package com.barrylanceleo.popularmovies.fragments;


import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.barrylanceleo.popularmovies.R;
import com.barrylanceleo.popularmovies.data.MovieContract;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class MovieDetailsFragment extends Fragment {

    //static final String LOG_TAG = MovieDetailsFragment.class.getSimpleName();
    private static final String MOVIE_ID = "movie_id";
    private int mMovieId;

    private View mRootView;
    private ProgressBar mBackDropProgressBar;
    private ImageView mBackdropImageView;
    private ImageView mFavImageView;
    private TextView mTitleTextView;
    private TextView mOverviewTextView;
    private ProgressBar mPosterProgressBar;
    private ImageView mPosterImageView;
    private TextView mReleaseDateTextView;
    private TextView mRatingsTextView;
    private Button mPhotosButton;
    private Button mVideosButton;
    private Button mReviewsButton;

    private boolean isFavorite;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface FragmentCallback {
        /**
         * FragmentCallback for when an item has been selected.
         */
        void onButtonSelected(Bundle selectedItemDetails);
    }

    public MovieDetailsFragment() {
        // Required empty public constructor
    }

    public static MovieDetailsFragment newInstance(int movieId) {
        MovieDetailsFragment fragment = new MovieDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(MOVIE_ID, movieId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMovieId = getArguments().getInt(MOVIE_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(mMovieId == -1) {
            // no movie has been selected yet
            return inflater.inflate(R.layout.fragment_movie_details_empty, container, false);
        }
        // Inflate the layout for this fragment
        mRootView =  inflater.inflate(R.layout.fragment_movie_details, container, false);

        mBackDropProgressBar = (ProgressBar) mRootView.findViewById(R.id.backdrop_image_details_progress);
        mBackdropImageView = (ImageView) mRootView.findViewById(R.id.backdropImageViewDetails);
        mTitleTextView = (TextView) mRootView.findViewById(R.id.movie_title_details);
        mFavImageView = (ImageView) mRootView.findViewById(R.id.fav_image_details);
        mOverviewTextView = (TextView) mRootView.findViewById(R.id.details_overview_text);
        mPosterProgressBar = (ProgressBar) mRootView.findViewById(R.id.poster_details_progress);
        mPosterImageView = (ImageView) mRootView.findViewById(R.id.poster_details_image_view);
        mReleaseDateTextView = (TextView) mRootView.findViewById(R.id.release_date_details);
        mRatingsTextView = (TextView) mRootView.findViewById(R.id.ratings_details);
        mPhotosButton = (Button) mRootView.findViewById(R.id.details_photos_button);
        mVideosButton = (Button) mRootView.findViewById(R.id.details_videos_button);
        mReviewsButton = (Button) mRootView.findViewById(R.id.details_reviews_button);

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

        if(mMovieId == -1) {
            // no movie has been selected yet
            return;
        }

        mBackDropProgressBar.setVisibility(View.VISIBLE);
        mPosterProgressBar.setVisibility(View.VISIBLE);

        // query the details from the database
        Cursor movieCursor = getContext().getContentResolver().query(
                MovieContract.MovieDetailsEntry.buildMovieDetailsUri(mMovieId),
                null,
                null,
                null,
                null);

        //Log.v(LOG_TAG, "Loading Cursor:" + DatabaseUtils.dumpCursorToString(movieCursor));
        if(movieCursor == null || movieCursor.getCount() == 0) {
            return;
        }

        movieCursor.moveToFirst();

        // load the backdrop image along with its progress bar
        Picasso.with(getContext()).load(movieCursor.getString(movieCursor.getColumnIndex(
                    MovieContract.MovieDetailsEntry.COLUMN_BACKDROP_URL)))
                .into(mBackdropImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        if (mBackDropProgressBar != null) {
                            mBackDropProgressBar.setVisibility(View.GONE);
                            mBackdropImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        }
                    }

                    @Override
                    public void onError() {
                        if (mBackDropProgressBar != null) {
                            mBackDropProgressBar.setVisibility(View.GONE);
                            mBackdropImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                            mBackdropImageView.setImageResource(R.drawable.ic_error_outline_black_36dp);
                        }
                    }
                });

        // set the movie title
        mTitleTextView.setText(movieCursor.getString(movieCursor.getColumnIndex(
                MovieContract.MovieDetailsEntry.COLUMN_TITLE)));

        // setup the fav button, check if its a favorite movie
        Cursor favCursor = getContext().getContentResolver().query(
                MovieContract.FavoriteMovieEntry.buildFavoriteMovieUri(mMovieId),
                null,
                null,
                null,
                null);

        if (favCursor == null || favCursor.getCount() ==  0) {
            isFavorite = false;
            mFavImageView.setImageResource(R.drawable.ic_favorite_border_black_48dp);
        }
        else {
            isFavorite = true;
            mFavImageView.setImageResource(R.drawable.ic_favorite_black_48dp);
        }

        if(favCursor != null) {
            favCursor.close();
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
                                    new String[]{Integer.toString(mMovieId)}
                            );
                            return null;
                        }
                    }.execute();

                    mFavImageView.setImageResource(R.drawable.ic_favorite_border_black_48dp);
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
                            favMovieCV.put(MovieContract.FavoriteMovieEntry.COLUMN_MOVIE_ID, mMovieId);
                            getContext().getContentResolver().insert(
                                    MovieContract.FavoriteMovieEntry.CONTENT_URI,
                                    favMovieCV
                            );
                            return null;
                        }
                    }.execute();
                    mFavImageView.setImageResource(R.drawable.ic_favorite_black_48dp);
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
        Picasso.with(getContext()).load(movieCursor.getString(movieCursor.getColumnIndex(
                    MovieContract.MovieDetailsEntry.COLUMN_POSTER_URL)))
                .into(mPosterImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        if (mPosterProgressBar != null) {
                            mPosterProgressBar.setVisibility(View.GONE);
                            mPosterImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        }
                    }

                    @Override
                    public void onError() {
                        if (mPosterProgressBar != null) {
                            mPosterProgressBar.setVisibility(View.GONE);
                            mPosterImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                            mPosterImageView.setImageResource(R.drawable.ic_error_outline_black_36dp);
                        }
                    }
                });

        // set the release date
        mReleaseDateTextView.append("\n" + movieCursor.getString(movieCursor.getColumnIndex(
                MovieContract.MovieDetailsEntry.COLUMN_RELEASE_DATE)));

        // set the ratings
        mRatingsTextView.append("\n" + movieCursor.getDouble(movieCursor.getColumnIndex(
                MovieContract.MovieDetailsEntry.COLUMN_VOTE_AVERAGE)) + "/10");

        // set up the photos, videos and reviews button
        mPhotosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // call the onMovieSelected of the containing activity
                Bundle selectedItemDetails = new Bundle();
                selectedItemDetails.putString("button", "photos");
                selectedItemDetails.putInt(MOVIE_ID, mMovieId);
                ((MovieDetailsFragment.FragmentCallback) getActivity()).onButtonSelected(selectedItemDetails);
            }
        });

        mVideosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // call the onMovieSelected of the containing activity
                Bundle selectedItemDetails = new Bundle();
                selectedItemDetails.putString("button", "videos");
                selectedItemDetails.putInt(MOVIE_ID, mMovieId);
                ((MovieDetailsFragment.FragmentCallback) getActivity()).onButtonSelected(selectedItemDetails);
            }
        });

        mReviewsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // call the onMovieSelected of the containing activity
                Bundle selectedItemDetails = new Bundle();
                selectedItemDetails.putString("button", "reviews");
                selectedItemDetails.putInt(MOVIE_ID, mMovieId);
                ((MovieDetailsFragment.FragmentCallback) getActivity()).onButtonSelected(selectedItemDetails);
            }
        });

        movieCursor.close();
    }

}
