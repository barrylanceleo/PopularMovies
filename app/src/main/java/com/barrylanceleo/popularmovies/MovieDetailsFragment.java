package com.barrylanceleo.popularmovies;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
    private ProgressBar backDropProgressBar;
    private ImageView backdropImageView;
    private TextView mTitleTextView;
    private TextView mOverviewTextView;
    private ProgressBar mPosterProgressBar;
    private ImageView mPosterImageView;
    private TextView mReleaseDateTextView;
    private TextView mRatingsTextView;

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

        backDropProgressBar = (ProgressBar) mRootView.findViewById(R.id.backdrop_image_details_progress);
        backdropImageView = (ImageView) mRootView.findViewById(R.id.backdropImageViewDetails);
        mTitleTextView = (TextView) mRootView.findViewById(R.id.movie_title_details);
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
        backDropProgressBar.setVisibility(View.VISIBLE);
        mPosterProgressBar.setVisibility(View.VISIBLE);

        // query the details from the database
        Cursor movieCursor = mContext.getContentResolver().query(
                MovieContract.MovieDetailsEntry.buildMovieDetailsUri(mMovie_id),
                null,
                null,
                null,
                null);

        //Log.v(LOG_TAG, "Loading Cursor:" + DatabaseUtils.dumpCursorToString(movieCursor));

        movieCursor.moveToFirst();

        // load the backdrop image along with its progress bar
        Picasso.with(mContext).load(movieCursor.getString(movieCursor.getColumnIndex(
                    MovieContract.MovieDetailsEntry.COLUMN_BACKDROP_URL)))
                .into(backdropImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        backDropProgressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        // NO-OP
                    }
                });

        // set the movie title
        mTitleTextView.setText(movieCursor.getString(movieCursor.getColumnIndex(
                MovieContract.MovieDetailsEntry.COLUMN_TITLE)));

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
