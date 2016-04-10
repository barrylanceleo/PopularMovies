package com.barrylanceleo.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.List;

public class MovieGridManager implements AbsListView.OnScrollListener, AdapterView.OnItemClickListener {

    private static final String TAG = MovieGridManager.class.getSimpleName();
    private int threshold;
    private int lastItem;
    private int pageToFetch;
    private String sortOrder;
    private MovieGridAdapter movieGridAdapter;
    private GridView imagesGridView;
    private MovieDbApiHelper movieDbHelper;
    private Context mContext;

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        Utility.setPreferredSortOrder(mContext, sortOrder);
        this.sortOrder = sortOrder;
    }

    public MovieGridAdapter getMovieGridAdapter() {
        return movieGridAdapter;
    }

    public void setMovieGridAdapter(MovieGridAdapter movieGridAdapter) {
        this.movieGridAdapter = movieGridAdapter;
    }

    MovieGridManager(Context mContext) {
        this.mContext = mContext;
        this.sortOrder = Utility.getPreferredSortOrder(mContext);
        movieDbHelper = new MovieDbApiHelper(mContext);
        lastItem = 0;
        threshold = 10;
        pageToFetch = 1;
        initGridView();
    }


    // create view and set up its listeners and adapter
    void initGridView() {
        imagesGridView = (GridView) ((Activity)mContext).findViewById(R.id.imagesGridView);
        movieGridAdapter = new MovieGridAdapter((Activity)mContext);
        imagesGridView.setAdapter(movieGridAdapter);
        imagesGridView.setOnItemClickListener(this);
        imagesGridView.setOnScrollListener(this);
    }

    void resetDataAndOptions() {
        imagesGridView = (GridView) ((Activity)mContext).findViewById(R.id.imagesGridView);
        movieGridAdapter.clear();
        imagesGridView.setAdapter(movieGridAdapter);
        imagesGridView.setOnItemClickListener(this);
        imagesGridView.setOnScrollListener(this);
        lastItem = 0;
        pageToFetch = 1;
    }


    List<Movie> fetchMovies() throws UnableToFetchData {
        List<Movie> movies;
        switch (sortOrder) {
            case "vote_average.desc":
                Bundle extraParameters = new Bundle();
                extraParameters.putString("vote_count.gte", "100");
                movies = movieDbHelper.getMovies(sortOrder, pageToFetch + "", extraParameters);
                break;
            default:
                movies = movieDbHelper.getMovies(sortOrder, pageToFetch + "");
        }
        Log.v(TAG, "Fetched Movies in " + sortOrder + " order from page " + pageToFetch);
        return movies;
    }

    void addMovies(List<Movie> movies) {
        Log.v(TAG, "Added page " + pageToFetch + " of movies to the list.");
        pageToFetch++;
        lastItem += movies.size();
        movieGridAdapter.addAll(movies);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // NO-OP
    }

    /**
     * Callback method to be invoked when the list or grid has been scrolled. This will be
     * called after the scroll has completed
     *
     * @param view             The view whose scroll state is being reported
     * @param firstVisibleItem the index of the first visible cell (ignore if
     *                         visibleItemCount == 0)
     * @param visibleItemCount the number of visible cells
     * @param totalItemCount   the number of items in the list adaptor
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//        Log.v(LOG_TAG, "First Visible Item: " + firstVisibleItem + " Last Visible Item: " + lastItem);
//        Log.v(LOG_TAG, "No. of items in dataset: " + movieGridAdapter.getCount());

        //add new movies if required
        while (firstVisibleItem + visibleItemCount >= lastItem - threshold) {
            try {
                addMovies(fetchMovies());
            } catch (UnableToFetchData e) {
                // if we reached the last item display a toast about lack of internet connection
                if (firstVisibleItem + visibleItemCount == totalItemCount &&
                        movieGridAdapter.getCount() != 0) {
                    Snackbar.make(((Activity)mContext).findViewById(R.id.imagesGridView),
                            mContext.getString(R.string.fetch_data_fail_message_1_line),
                            Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
                return;
            }
        }
    }


    /**
     * Callback method to be invoked when an item in this AdapterView has
     * been clicked.
     * <p/>
     * Implementers can call getItemAtPosition(position) if they need
     * to access the data associated with the selected item.
     *
     * @param parent   The AdapterView where the click happened.
     * @param view     The view within the AdapterView that was clicked (this
     *                 will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id       The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Movie aMovie = movieGridAdapter.getItem(position);

        // make a movie details bundle
        Bundle movieDetailsBundle = new Bundle();
        movieDetailsBundle.putString("title", aMovie.getTitle());
        movieDetailsBundle.putString("posterUrl", aMovie.getPosterUrl());
        movieDetailsBundle.putString("backdropUrl", aMovie.getBackdropUrl());
        movieDetailsBundle.putString("overview", aMovie.getOverview());
        movieDetailsBundle.putDouble("vote_average", aMovie.getVote_average());
        movieDetailsBundle.putString("release_date", aMovie.getRelease_date());

        // call the onItemSelected of the containing activity
        ((MovieGridFragment.Callback) mContext).onItemSelected(movieDetailsBundle);
    }
}
