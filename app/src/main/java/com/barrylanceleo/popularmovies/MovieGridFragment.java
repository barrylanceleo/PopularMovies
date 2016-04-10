package com.barrylanceleo.popularmovies;



import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class MovieGridFragment extends Fragment {

    static final String LOG_TAG = MovieGridFragment.class.getSimpleName();

    MovieGridManager mMovieGridManager;
    Context mContext;
    View mRootView;
    SwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(Bundle movieDetails);
    }

    public MovieGridFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null (which
     * is the default implementation).  This will be called between
     * {@link #onCreate(Bundle)} and {@link #onActivityCreated(Bundle)}.
     * <p/>
     * <p>If you return a View from here, you will later be called in
     * {@link #onDestroyView} when the view is being released.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_movie_grid, container, false);

        // setup the swipe refresh action
        mSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.MovieGridSwipeRefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                // load data
                loadMoviesFirstTime();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        return mRootView;
    }

    /**
     * Called when the fragment's activity has been created and this
     * fragment's view hierarchy instantiated.  It can be used to do final
     * initialization once these pieces are in place, such as retrieving
     * views or restoring state.  It is also useful for fragments that use
     * {@link #setRetainInstance(boolean)} to retain their instance,
     * as this callback tells the fragment when it is fully associated with
     * the new activity instance.  This is called after {@link #onCreateView}
     * and before {@link #onViewStateRestored(Bundle)}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //create a grid manager which creates a grid view and implements its listeners and handles data
        mContext = getContext();
        mMovieGridManager = new MovieGridManager(mContext);

        // load data
        loadMoviesFirstTime();
    }

    public void loadMoviesFirstTime() {
        LinearLayout noInternetLayout = (LinearLayout) mRootView.findViewById(R.id.noInternetLayout);
        try {
            mMovieGridManager.addMovies(mMovieGridManager.fetchMovies());
            noInternetLayout.setVisibility(View.INVISIBLE);
        } catch (UnableToFetchData e) {
            if (mMovieGridManager.getMovieGridAdapter().getCount() == 0)
                noInternetLayout.setVisibility(View.VISIBLE);
            Snackbar.make(mRootView.findViewById(R.id.imagesGridView),
                    getString(R.string.fetch_data_fail_message_1_line),
                    Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movies_grid_options, menu);
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
                AlertDialog.Builder chooseSortOrderBuilder = new AlertDialog.Builder(mContext);
                CharSequence[] sortOrderChoices = new String[]{"Most Popular", "Most Highly Rated"};
                int currentChoice = mMovieGridManager.getSortOrder()
                        .equalsIgnoreCase("popularity.desc") ? 0 : 1;
                chooseSortOrderBuilder.setTitle("Select Sort Order");
                chooseSortOrderBuilder.setSingleChoiceItems(sortOrderChoices, currentChoice,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                switch (which) {
                                    case 0:
                                        if (mMovieGridManager.getSortOrder().equalsIgnoreCase("popularity.desc")) {
                                            Snackbar.make(mRootView.findViewById(R.id.imagesGridView),
                                                    "You are already in the most-popular view.",
                                                    Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                            return;
                                        }
                                        Log.v(LOG_TAG, "Selected order popularity.desc");
                                        mMovieGridManager.setSortOrder("popularity.desc");
                                        break;
                                    case 1:
                                        Log.v(LOG_TAG, "Selected order vote_average.desc");
                                        if (mMovieGridManager.getSortOrder().equalsIgnoreCase("vote_average.desc")) {
                                            Snackbar.make(mRootView.findViewById(R.id.imagesGridView),
                                                    "You are already in the highest-rated view.",
                                                    Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                            return;
                                        }
                                        mMovieGridManager.setSortOrder("vote_average.desc");
                                        break;
                                }
                                mMovieGridManager.resetDataAndOptions();
                                //load data
                                loadMoviesFirstTime();
                            }
                        });
                chooseSortOrderBuilder.create();
                chooseSortOrderBuilder.show();
                break;

            default:
                Snackbar.make(mRootView.findViewById(R.id.imagesGridView), "Invalid Button.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
        }
        return super.onOptionsItemSelected(item);
    }

}
