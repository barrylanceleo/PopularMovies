package com.barrylanceleo.popularmovies.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.barrylanceleo.popularmovies.MovieDbApiHelper;
import com.barrylanceleo.popularmovies.R;
import com.barrylanceleo.popularmovies.adapters.ReviewListAdapter;

import org.json.JSONObject;

import java.util.List;

public class ReviewListFragment extends Fragment {
    //private static final String LOG_TAG = ReviewListFragment.class.getSimpleName();

    private static final String MOVIE_ID = "movie_id";
    private int mMovieId;
    private ListView mReviewListView;
    private ReviewListAdapter mReviewListAdapter;
    private SwipeRefreshLayout mReviewSwipeRefreshLayout;
    private TextView mNoReviewsTextView;
    private MovieDbApiHelper mMovieDbHelper;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ReviewListFragment() {
    }

    public static ReviewListFragment newInstance(int movieId) {
        ReviewListFragment fragment = new ReviewListFragment();
        Bundle args = new Bundle();
        args.putInt(MOVIE_ID, movieId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMovieId = getArguments().getInt(MOVIE_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_review_list, container, false);
        mReviewSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.review_swipreRefresh);
        mReviewSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshReviews(mMovieId);
            }
        });
        mReviewListView = (ListView) rootView.findViewById(R.id.review_list);
        mNoReviewsTextView = (TextView) rootView.findViewById(R.id.review_empty_textView);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mReviewListAdapter = new ReviewListAdapter(getContext(), R.layout.list_item_review);
        mReviewListView.setAdapter(mReviewListAdapter);
        mMovieDbHelper = MovieDbApiHelper.getInstance(getContext().getResources().getString(R.string.api_key));
        refreshReviews(mMovieId);
    }

    private void refreshReviews(final int movieId) {
        mReviewSwipeRefreshLayout.setRefreshing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<JSONObject> reviewList = mMovieDbHelper.getReviews(movieId, 1);
                    ReviewListFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mReviewListAdapter.clear();
                            mReviewListAdapter.addAll(reviewList);
                        }
                    });
                    onRefreshCompleted(reviewList.size());
                }
                catch (MovieDbApiHelper.UnableToFetchDataException e) {
                    onRefreshCompleted(-1);
                }
            }
        }).start();
    }

    private void onRefreshCompleted(final int reviewCount) {
        ReviewListFragment.this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(reviewCount <= 0) {
                    // no reviews
                    mNoReviewsTextView.setVisibility(View.VISIBLE);
                    mReviewListView.setVisibility(View.GONE);
                    if(reviewCount < 0) {
                        // no internet
                        Snackbar.make(mNoReviewsTextView,
                                getString(R.string.no_internet) +" " +getString(R.string.refresh_direction),
                                Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                }
                else {
                    mNoReviewsTextView.setVisibility(View.GONE);
                    mReviewListView.setVisibility(View.VISIBLE);
                }
                mReviewSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

}
