package com.barrylanceleo.popularmovies.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.barrylanceleo.popularmovies.fragments.ImageListFragment;
import com.barrylanceleo.popularmovies.fragments.MovieDetailsFragment;
import com.barrylanceleo.popularmovies.R;
import com.barrylanceleo.popularmovies.fragments.ReviewListFragment;
import com.barrylanceleo.popularmovies.fragments.VideoListFragment;

public class DetailsActivity extends AppCompatActivity implements MovieDetailsFragment.FragmentCallback {
    private static final String LOG_TAG = MovieDetailsFragment.class.getSimpleName();
    private int mMovieId;
    private int mFragmentLevel;

    private static final String MOVIE_ID = "movie_id";

    // tags for fragments
    private final static String DETAIL_FRAGMENT_TAG = "DFTAG";
    private final static String IMAGE_FRAGMENT_TAG = "IFTAG";
    private final static String VIDEO_FRAGMENT_TAG = "VFTAG";
    private final static String REVIEW_FRAGMENT_TAG = "RFTAG";

    // tags to save state
    private final static String FRAGMENT_LEVEL_TAG = "FLTAG";

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     * <p/>
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.</p>
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                Log.i(LOG_TAG, "Home button clicked");
                mFragmentLevel--;
                // move to the previous fragment
                if(mFragmentLevel == 1) {
                    setTitle("About the movie");
                    getSupportFragmentManager().popBackStackImmediate();
                    return true;
                }

                // else move to the home screen
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                // if the parent activity needs to be recreated, create it
                // otherwise bring back the already existing activity
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
                }
                upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(upIntent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Log.i(LOG_TAG, "Back button clicked");
        mFragmentLevel--;
        if(mFragmentLevel == 1) {
            setTitle("About the movie");
        }
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
        // get the extras from Intent
        Intent mIntent = getIntent();
        mMovieId = mIntent.getIntExtra(MOVIE_ID, -1);

        if(savedInstanceState == null) {
            MovieDetailsFragment movieDetailsFragment = MovieDetailsFragment.newInstance(mMovieId);
            mFragmentLevel++;
            setTitle("About the movie");
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, movieDetailsFragment, DETAIL_FRAGMENT_TAG)
                    .commit();
        }
    }

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
                mFragmentLevel++;
                setTitle("Photos");
                getSupportFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.movie_detail_container, imageListFragment, IMAGE_FRAGMENT_TAG)
                        .commit();
                break;
            case "videos":
                VideoListFragment videoListFragment = VideoListFragment.newInstance(currentMovieId);
                mFragmentLevel++;
                setTitle("Videos");
                getSupportFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.movie_detail_container, videoListFragment, VIDEO_FRAGMENT_TAG)
                        .commit();
                break;
            case "reviews":
                ReviewListFragment reviewListFragment = ReviewListFragment.newInstance(currentMovieId);
                mFragmentLevel++;
                setTitle("Reviews");
                getSupportFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.movie_detail_container, reviewListFragment, REVIEW_FRAGMENT_TAG)
                        .commit();
                break;
            default:
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(FRAGMENT_LEVEL_TAG, mFragmentLevel);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null) {
            mFragmentLevel = savedInstanceState.getInt(FRAGMENT_LEVEL_TAG);
        }
    }
}
