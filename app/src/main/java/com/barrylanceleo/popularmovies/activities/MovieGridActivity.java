package com.barrylanceleo.popularmovies.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.barrylanceleo.popularmovies.fragments.MovieGridFragment;
import com.barrylanceleo.popularmovies.R;

public class MovieGridActivity extends AppCompatActivity implements MovieGridFragment.Callback
{

    //static final String LOG_TAG = MovieGridActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_grid);
    }

    /**
     * DetailFragmentCallback for when an item has been selected.
     **/
    @Override
    public void onItemSelected(Bundle movieDetails) {

        // add the bundle to the intent and start the details activity
        Intent openDetailsIntent = new Intent(this, MovieDetailsActivity.class);
        openDetailsIntent.putExtras(movieDetails);
        startActivity(openDetailsIntent);

    }
}
