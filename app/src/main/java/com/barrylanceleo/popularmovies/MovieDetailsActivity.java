package com.barrylanceleo.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

public class MovieDetailsActivity extends AppCompatActivity implements MovieDetailsFragment.FragmentCallback {
    static final String LOG_TAG = MovieDetailsFragment.class.getSimpleName();
    private int mMovie_id = 0;
    private int mFragmentLevel = 0;

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
        setContentView(R.layout.activity_movie_details);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        // get the extras from Intent
        Intent mIntent = getIntent();
        Bundle movieDetailsBundle = mIntent.getExtras();
        mMovie_id = movieDetailsBundle.getInt("movie_id");
        MovieDetailsFragment movieDetailsFragment = new MovieDetailsFragment();
        movieDetailsFragment.setArguments(movieDetailsBundle);
        mFragmentLevel++;
        setTitle("About the movie");
        getSupportFragmentManager().beginTransaction()
                .add(R.id.movie_detail_container, movieDetailsFragment)
                .commit();
    }

    @Override
    public void onItemSelected(Bundle selectedItemDetails) {
        String selectedItem = selectedItemDetails.getString("button");
        if(selectedItem == null) {
            return;
        }
        switch (selectedItem) {
            case "photos":
                break;
            case "videos":
                break;
            case "reviews":
                ReviewFragment reviewFragment = ReviewFragment.newInstance(mMovie_id);
                mFragmentLevel++;
                setTitle("Reviews");
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.movie_detail_container, reviewFragment)
                        .addToBackStack(null)
                        .commit();
                break;
            default:
                return;
        }
    }
}
