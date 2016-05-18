package com.barrylanceleo.popularmovies.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.barrylanceleo.popularmovies.adapters.MovieGridAdapter;
import com.barrylanceleo.popularmovies.data.MovieContract;
import com.barrylanceleo.popularmovies.fragments.ImageListFragment;
import com.barrylanceleo.popularmovies.fragments.MovieDetailsFragment;
import com.barrylanceleo.popularmovies.fragments.MovieGridFragment;
import com.barrylanceleo.popularmovies.R;
import com.barrylanceleo.popularmovies.fragments.ReviewListFragment;
import com.barrylanceleo.popularmovies.fragments.VideoListFragment;

public class HomeActivity extends AppCompatActivity implements MovieGridFragment.FragmentCallback, MovieDetailsFragment.FragmentCallback
{

    private static final String LOG_TAG = HomeActivity.class.getSimpleName();
    private boolean mTwoPane;
    private static final String MOVIE_ID = "movie_id";

    // tags for fragments
    private final static String DETAIL_FRAGMENT_TAG = "DFTAG";
    private final static String IMAGE_FRAGMENT_TAG = "IFTAG";
    private final static String VIDEO_FRAGMENT_TAG = "VFTAG";
    private final static String REVIEW_FRAGMENT_TAG = "RFTAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // find the number of panes in the display
        if(findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;
            if(savedInstanceState == null) {
                MovieDetailsFragment movieDetailsFragment = MovieDetailsFragment.newInstance(-1);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, movieDetailsFragment, DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        }
        else {
            Log.e(LOG_TAG, "One pane view");
            mTwoPane = false;
        }
    }

    /**
     * DetailFragmentCallback for when an item has been selected.
     **/
    @Override
    public void onMovieSelected(int movieId) {

        // start the details activity if its a single pane
        // else update the details fragment
        if(mTwoPane) {
            MovieDetailsFragment movieDetailsFragment = MovieDetailsFragment.newInstance(movieId);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, movieDetailsFragment, DETAIL_FRAGMENT_TAG)
                    .commit();
        }
        else {
            Intent openDetailsIntent = new Intent(this, DetailsActivity.class);
            openDetailsIntent.putExtra(MOVIE_ID, movieId);
            startActivity(openDetailsIntent);
        }
    }

    // callback from movie grid fragment
    @Override
    public void onRefreshCompleted(MovieGridAdapter gridAdapter) {
        // display the fragment of the first movie if it is a two pane display
        if(mTwoPane) {
            Cursor movieCursor = (Cursor)gridAdapter.getItem(0);
            if(movieCursor != null && movieCursor.getCount() != 0) {
                int firstMovieId = movieCursor.getInt(movieCursor.getColumnIndex(
                        MovieContract.MovieDetailsEntry.COLUMN_MOVIE_ID));
                Log.i(LOG_TAG, "First Movie: " +firstMovieId);
                MovieDetailsFragment movieDetailsFragment = MovieDetailsFragment.newInstance(firstMovieId);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, movieDetailsFragment, DETAIL_FRAGMENT_TAG)
                        .commit();
            }
            else {
                // display an empty fragment
                MovieDetailsFragment movieDetailsFragment = MovieDetailsFragment.newInstance(-1);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, movieDetailsFragment, DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        }
        Log.i(LOG_TAG, "Refresh Completed");
    }

    // callback from movie details fragment
    @Override
    public void onButtonSelected(Bundle selectedButtonDetails) {
        String selectedItem = selectedButtonDetails.getString("button");
        int currentMovieId = selectedButtonDetails.getInt(MOVIE_ID);
        if(selectedItem == null) {
            return;
        }
        switch (selectedItem) {
            case "photos":
                ImageListFragment imageListFragment = ImageListFragment.newInstance(currentMovieId);
                getSupportFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.movie_detail_container, imageListFragment, IMAGE_FRAGMENT_TAG)
                        .commit();
                break;
            case "videos":
                VideoListFragment videoListFragment = VideoListFragment.newInstance(currentMovieId);
                getSupportFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.movie_detail_container, videoListFragment, VIDEO_FRAGMENT_TAG)
                        .commit();
                break;
            case "reviews":
                ReviewListFragment reviewListFragment = ReviewListFragment.newInstance(currentMovieId);
                getSupportFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.movie_detail_container, reviewListFragment, REVIEW_FRAGMENT_TAG)
                        .commit();
                break;
            default:
                return;
        }
    }
}
