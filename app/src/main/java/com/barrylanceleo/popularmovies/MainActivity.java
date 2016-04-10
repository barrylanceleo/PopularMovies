package com.barrylanceleo.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements MovieGridFragment.Callback
{

    static final String LOG_TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * DetailFragmentCallback for when an item has been selected.
     *
     * @param movieDetails
     */
    @Override
    public void onItemSelected(Bundle movieDetails) {

        // add the bundle to the intent and start the details activity
        Intent openDetailsIntent = new Intent(this, MovieDetailsActivity.class);
        openDetailsIntent.putExtras(movieDetails);
        startActivity(openDetailsIntent);

    }
}
