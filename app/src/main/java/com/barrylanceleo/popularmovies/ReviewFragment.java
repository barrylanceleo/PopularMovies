package com.barrylanceleo.popularmovies;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.json.JSONObject;

import java.util.List;

public class ReviewFragment extends Fragment {
    static final String LOG_TAG = ReviewFragment.class.getSimpleName();

    private static final String MOVIE_ID = "movie_id";
    private int mMovieId;
    private ListView mReviewListView;
    private ReviewListAdapter mReviewListAdapter;
    private SwipeRefreshLayout mReviewSwipeRefreshLayout;
    private MovieDbApiHelper mMovieDbHelper;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ReviewFragment() {
    }

    public static ReviewFragment newInstance(int movieId) {
        ReviewFragment fragment = new ReviewFragment();
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
        mReviewListView = (ListView) rootView.findViewById(R.id.review_list);
        mReviewSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.review_swipreRefresh);
        mReviewSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshReviews(mMovieId);
            }
        });
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

    void refreshReviews(final int movieId) {
        mReviewSwipeRefreshLayout.setRefreshing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<JSONObject> reviewList = mMovieDbHelper.getReviews(movieId, 1);
                if(reviewList.size() != 0) {
                    ReviewFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mReviewListAdapter.clear();
                            mReviewListAdapter.addAll(reviewList);
                        }
                    });
                }
                onRefreshCompleted(0);
            }
        }).start();
    }

    void onRefreshCompleted(int reviewCount) {
        ReviewFragment.this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mReviewSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

}
