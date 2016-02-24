package com.barrylanceleo.popularmovies;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;

public class MainActivity extends AppCompatActivity {

    static final String TAG = MainActivity.class.getSimpleName();
    ImageGridManager mImageGridManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set up the grid view to display
        GridView imagesGridView = (GridView) findViewById(R.id.imagesGridView);
        ImageGridAdapter imageGridAdapter = new ImageGridAdapter(this);
        imagesGridView.setAdapter(imageGridAdapter);

        //create a grid manager which implements its listeners and handles data
        mImageGridManager = new ImageGridManager(this, imageGridAdapter,
                getString(R.string.initial_sort_order));
        imagesGridView.setOnItemClickListener(mImageGridManager);
        imagesGridView.setOnScrollListener(mImageGridManager);

        //load data
        mImageGridManager.addMovies(mImageGridManager.fetchMovies());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.movies_grid_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
//            case R.id.action_settings:
//                Snackbar.make(findViewById(R.id.imagesGridView), "This should open the settings", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//                break;

            case R.id.action_sort_order:

                String newSortOrder = mImageGridManager.getSortOrder().equalsIgnoreCase("popularity.desc") ?
                        "vote_average.desc" : "popularity.desc";
                mImageGridManager.resetDataAndOptions(newSortOrder);
                mImageGridManager.addMovies(mImageGridManager.fetchMovies());

                switch (mImageGridManager.getSortOrder()) {
                    case "popularity.desc":
                        Snackbar.make(findViewById(R.id.imagesGridView), "These are the most popular movies.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        break;
                    case "vote_average.desc":
                        Snackbar.make(findViewById(R.id.imagesGridView), "These are the most highly rated movies.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        break;
                    default:
                        Snackbar.make(findViewById(R.id.imagesGridView), "UnHandled sortOrder.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                }
                break;

            default:
                Snackbar.make(findViewById(R.id.imagesGridView), "Invalid Button.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
        }
        return super.onOptionsItemSelected(item);
    }

}
