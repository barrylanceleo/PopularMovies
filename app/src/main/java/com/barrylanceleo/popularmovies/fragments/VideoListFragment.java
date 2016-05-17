package com.barrylanceleo.popularmovies.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.barrylanceleo.popularmovies.MovieDbApiHelper;
import com.barrylanceleo.popularmovies.R;
import com.barrylanceleo.popularmovies.adapters.ReviewListAdapter;
import com.barrylanceleo.popularmovies.adapters.VideoListAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.AbstractList;
import java.util.List;

public class VideoListFragment extends Fragment {
    private static final String LOG_TAG = VideoListFragment.class.getSimpleName();

    private static final String MOVIE_ID = "movie_id";

    private int mMovieId;
    private ListView mVideoListView;
    private VideoListAdapter mVideoListAdapter;
    private SwipeRefreshLayout mVideoSwipeRefreshLayout;
    private TextView mNoVideoTextView;
    private MovieDbApiHelper mMovieDbHelper;

    public VideoListFragment() {
        // Required empty public constructor
    }

    public static VideoListFragment newInstance(int movieId) {
        VideoListFragment fragment = new VideoListFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_video_list, container, false);
        mVideoSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.video_swipreRefresh);
        mVideoSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshVideos(mMovieId);
            }
        });
        mVideoListView = (ListView) rootView.findViewById(R.id.video_list);
        mNoVideoTextView = (TextView) rootView.findViewById(R.id.video_empty_textView);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mVideoListAdapter = new VideoListAdapter(getContext(), R.layout.list_item_video);
        mVideoListView.setAdapter(mVideoListAdapter);
        mMovieDbHelper = MovieDbApiHelper.getInstance(getContext().getResources().getString(R.string.api_key));
        refreshVideos(mMovieId);
    }

    void refreshVideos(final int movieId) {
        mVideoSwipeRefreshLayout.setRefreshing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<JSONObject> videoList = mMovieDbHelper.getVideos(movieId);
                    VideoListFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mVideoListAdapter.clear();
                            mVideoListAdapter.addAll(videoList);
                        }
                    });
                    onRefreshCompleted(videoList.size());
                }
                catch (MovieDbApiHelper.UnableToFetchDataException e) {
                    onRefreshCompleted(-1);
                }
            }
        }).start();
    }

    void onRefreshCompleted(final int videoCount) {
        VideoListFragment.this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(videoCount <= 0) {
                    // no reviews
                    mNoVideoTextView.setVisibility(View.VISIBLE);
                    mVideoListView.setVisibility(View.GONE);

                    if(videoCount < 0) {
                        // no internet
                        Snackbar.make(mNoVideoTextView,
                                getString(R.string.no_internet) +" " +getString(R.string.refresh_direction),
                                Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                }
                else {
                    mNoVideoTextView.setVisibility(View.GONE);
                    mVideoListView.setVisibility(View.VISIBLE);
                }
                mVideoSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}
