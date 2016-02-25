package com.barrylanceleo.popularmovies;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.List;

public class ImageGridManager implements AbsListView.OnScrollListener, AdapterView.OnItemClickListener {

    private static final String TAG = ImageGridManager.class.getSimpleName();
    private int threshold;
    private int lastItem;
    private int pageToFetch;
    private ImageGridAdapter imageGridAdapter;
    private GridView imagesGridView;
    private MovieDbApiHelper movieDbHelper;
    private String sortOrder;
    private Activity mContext;

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public ImageGridAdapter getImageGridAdapter() {
        return imageGridAdapter;
    }

    public void setImageGridAdapter(ImageGridAdapter imageGridAdapter) {
        this.imageGridAdapter = imageGridAdapter;
    }

    ImageGridManager(Activity mContext, String sortOrder) {
        this.mContext = mContext;
        this.sortOrder = sortOrder;
        movieDbHelper = new MovieDbApiHelper(mContext);
        lastItem = 0;
        threshold = 10;
        pageToFetch = 1;
        initGridView();
    }


    // create view and set up its listeners and adapter
    void initGridView() {
        imagesGridView = (GridView) mContext.findViewById(R.id.imagesGridView);
        imageGridAdapter = new ImageGridAdapter(mContext);
        imagesGridView.setAdapter(imageGridAdapter);
        imagesGridView.setOnItemClickListener(this);
        imagesGridView.setOnScrollListener(this);
    }

    void resetDataAndOptions(String sortOrder) {
        imagesGridView = (GridView) mContext.findViewById(R.id.imagesGridView);
        imageGridAdapter.clear();
        imagesGridView.setAdapter(imageGridAdapter);
        imagesGridView.setOnItemClickListener(this);
        imagesGridView.setOnScrollListener(this);
        this.sortOrder = sortOrder;
        lastItem = 0;
        pageToFetch = 1;
    }


    List<Movie> fetchMovies() throws NoInternetException {
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
        imageGridAdapter.addAll(movies);
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
//        Log.v(TAG, "First Visible Item: " + firstVisibleItem + " Last Visible Item: " + lastItem);
//        Log.v(TAG, "No. of items in dataset: " + imageGridAdapter.getCount());

        //add new movies if required
        while (firstVisibleItem + visibleItemCount >= lastItem - threshold) {
            try {
                addMovies(fetchMovies());
            } catch (NoInternetException e) {
                // if we reached the last item display a toast about lack of internet connection
                if (firstVisibleItem + visibleItemCount == totalItemCount &&
                        imageGridAdapter.getCount() != 0) {
                    Snackbar.make(mContext.findViewById(R.id.imagesGridView),
                            mContext.getString(R.string.no_internet_message_single),
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

        Movie aMovie = imageGridAdapter.getItem(position);

        // make a movie details bundle
        Bundle movieDetailsBundle = new Bundle();
        movieDetailsBundle.putString("title", aMovie.getTitle());
        movieDetailsBundle.putString("posterUrl", aMovie.getPosterUrl());
        movieDetailsBundle.putString("backdropUrl", aMovie.getBackdropUrl());
        movieDetailsBundle.putString("overview", aMovie.getOverview());
        movieDetailsBundle.putDouble("vote_average", aMovie.getVote_average());
        movieDetailsBundle.putString("release_date", aMovie.getRelease_date());

        // add the bundle to the intent and start the details activity
        Intent openDetailsIntent = new Intent(mContext, MovieDetails.class);
        openDetailsIntent.putExtras(movieDetailsBundle);
        mContext.startActivity(openDetailsIntent);
    }
}
