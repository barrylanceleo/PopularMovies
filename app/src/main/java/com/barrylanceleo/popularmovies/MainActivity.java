package com.barrylanceleo.popularmovies;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    static final String TAG = MainActivity.class.getSimpleName();
    ImageGridManager mImageGridManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //create a grid manager which creates a grid view and implements its listeners and handles data
        mImageGridManager = new ImageGridManager(this, getString(R.string.initial_sort_order));

        // set-up refresh button
        Button reTry = (Button) findViewById(R.id.retry);
        reTry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // load data
                loadMoviesFirstTime();
            }
        });

        // load data
        loadMoviesFirstTime();
    }

    public void loadMoviesFirstTime() {
        LinearLayout noInternetLayout = (LinearLayout) findViewById(R.id.noInternetLayout);
        try {
            mImageGridManager.addMovies(mImageGridManager.fetchMovies());
            noInternetLayout.setVisibility(View.INVISIBLE);
        } catch (NoInternetException e) {
            if (mImageGridManager.getImageGridAdapter().getCount() == 0)
                noInternetLayout.setVisibility(View.VISIBLE);
            Snackbar.make(findViewById(R.id.imagesGridView),
                    "No Internet Connection",
                    Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
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

                //create and display a dialog to choose the sort order
                AlertDialog.Builder chooseSortOrderBuilder = new AlertDialog.Builder(this);
                CharSequence[] sortOrderChoices = new String[]{"Most Popular", "Most Highly Rated"};
                int currentChoice = mImageGridManager.getSortOrder()
                        .equalsIgnoreCase("popularity.desc") ? 0 : 1;
                chooseSortOrderBuilder.setTitle("Select Sort Order");
                chooseSortOrderBuilder.setSingleChoiceItems(sortOrderChoices, currentChoice,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                switch (which) {
                                    case 0:
                                        if (mImageGridManager.getSortOrder().equalsIgnoreCase("popularity.desc")) {
                                            Snackbar.make(findViewById(R.id.imagesGridView),
                                                    "You are already in the most-popular view.",
                                                    Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                            return;
                                        }
                                        Log.v(TAG, "Selected order popularity.desc");
                                        mImageGridManager.setSortOrder("popularity.desc");
                                        break;
                                    case 1:
                                        Log.v(TAG, "Selected order vote_average.desc");
                                        if (mImageGridManager.getSortOrder().equalsIgnoreCase("vote_average.desc")) {
                                            Snackbar.make(findViewById(R.id.imagesGridView),
                                                    "You are already in the highest-rated view.",
                                                    Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                            return;
                                        }
                                        mImageGridManager.setSortOrder("vote_average.desc");
                                        break;
                                }
                                mImageGridManager.resetDataAndOptions(mImageGridManager.getSortOrder());
                                //load data
                                loadMoviesFirstTime();
                            }
                        });
                chooseSortOrderBuilder.create();
                chooseSortOrderBuilder.show();
                break;

            default:
                Snackbar.make(findViewById(R.id.imagesGridView), "Invalid Button.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
        }
        return super.onOptionsItemSelected(item);
    }

}
