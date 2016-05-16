package com.barrylanceleo.popularmovies;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.MergeCursor;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.barrylanceleo.popularmovies.data.MovieContract;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class MovieGridFragment extends Fragment implements AbsListView.OnScrollListener, AdapterView.OnItemClickListener{

    static final String LOG_TAG = MovieGridFragment.class.getSimpleName();

    private Context mContext;
    private View mRootView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayout mNoMoviesLayout;
    private final int MOVIEGRID_THRESHOLD = 10;
    private int movieGrid_nextPageNum = 1;
    private String movieGrid_type;
    private MovieGridAdapter mMovieGridAdapter;
    private GridView mMoviesGridView;
    private MovieDbApiHelper mMovieDbHelper;

    // keep track of the internet connectivity
    private boolean isOnline = false;

    // keep track of movie addition to the grid
    private boolean isAdditionInProgress = false;

    // keep track of refresh
    private boolean isRefreshing = false;


    // Grid Types
    public static final String GRID_TYPE_POPULAR = "popularity.desc";
    public static final String GRID_TYPE_RATING = "vote_average.desc";
    public static final String GRID_TYPE_FAVORITE = "favorites";
    public static final String [] GRID_TYPES = new String[]{"Most Popular",
            "Top Rated", "Favorites"};

    public String getMovieGrid_type() {
        return movieGrid_type;
    }

    public void setMovieGrid_type(String movieGrid_type) {
        Utility.setPreferredSortOrder(mContext, movieGrid_type);
        this.movieGrid_type = movieGrid_type;
    }

    public MovieGridAdapter getmMovieGridAdapter() {
        return mMovieGridAdapter;
    }

    public void setmMovieGridAdapter(MovieGridAdapter mMovieGridAdapter) {
        this.mMovieGridAdapter = mMovieGridAdapter;
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * FragmentCallback for when an item has been selected.
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_movie_grid, container, false);

        mNoMoviesLayout = (LinearLayout) mRootView.findViewById(R.id.noMoviesLayout);

        // setup the swipe refresh action
        mSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.MovieGridSwipeRefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                // load data
                OnRefresh();
            }
        });

        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //create a grid manager which creates a grid view and implements its listeners and handles data
        mContext = getContext();
        movieGrid_type = Utility.getPreferredSortOrder(mContext);
        mMovieDbHelper = MovieDbApiHelper.getInstance(mContext.getResources().getString(R.string.api_key));
        initializeMoviesGrid();

    }

    // create view and set up its listeners and load data from Database
    void initializeMoviesGrid() {
        mMoviesGridView = (GridView) ((Activity)mContext).findViewById(R.id.imagesGridView);
        mMovieGridAdapter = new MovieGridAdapter(mContext, null, 0);
        mMoviesGridView.setAdapter(mMovieGridAdapter);
        mMoviesGridView.setOnItemClickListener(this);
        mMoviesGridView.setOnScrollListener(this);
        // load data
        OnRefresh();
    }

    public void OnRefresh() {

        if(isRefreshing) {
            return;
        }
        isRefreshing = true;
        // set up the activity title
        switch (getMovieGrid_type()) {
            case GRID_TYPE_POPULAR:
                getActivity().setTitle(GRID_TYPES[0]);
                break;
            case GRID_TYPE_RATING:
                getActivity().setTitle(GRID_TYPES[1]);
                break;
            case GRID_TYPE_FAVORITE:
                getActivity().setTitle(GRID_TYPES[2]);
                break;
            default:
                getActivity().setTitle(GRID_TYPES[0]);
        }

        // Signal SwipeRefreshLayout to start the progress indicator
        mSwipeRefreshLayout.setRefreshing(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                // if grid type is favorites just load from the database
                if(getMovieGrid_type().equalsIgnoreCase(GRID_TYPE_FAVORITE)) {
                    onRefreshComplete(loadDataFromDatabase());
                    return;
                }

                // else try to fetch content from the internet
                try {
                    final ContentValues[] moviesCv;
                    movieGrid_nextPageNum = 1;
                    moviesCv = fetchMovies(1);
                    clearDataFromDatabase(movieGrid_type);

                    // insert into the database
                    insertIntoDb(movieGrid_type, moviesCv);
                    // clear and add the new movies to the adapter
                    MovieGridFragment.this.getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            mMovieGridAdapter.changeCursor(null);
                            mMoviesGridView.setAdapter(mMovieGridAdapter);
                            addMoviesToAdapter(moviesCv);
                        }
                    });
                    isOnline = true;
                    onRefreshComplete(moviesCv.length);
                    return;
                } catch (UnableToFetchData e) {
                    isOnline = false;
                    Log.e(LOG_TAG, "Unable to fetch data.");
                    // if we are not online load data from the database
                    Snackbar.make(mRootView.findViewById(R.id.imagesGridView),
                            getString(R.string.no_internet) +" " +getString(R.string.refresh_direction),
                            Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    onRefreshComplete(loadDataFromDatabase());
                    return;
                }
            }
        }).start();
    }

    void onRefreshComplete(final int movieCount) {
        MovieGridFragment.this.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if (movieCount <= 0) {
                    mNoMoviesLayout.setVisibility(View.VISIBLE);
                }
                else {
                    mNoMoviesLayout.setVisibility(View.INVISIBLE);
                }
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        isRefreshing = false;
    }

    int loadDataFromDatabase() {
        Log.i(LOG_TAG, "Start Loading data from database.");
        final Cursor moviesCursor;
        switch (getMovieGrid_type()) {
            case GRID_TYPE_POPULAR:
                moviesCursor = mContext.getContentResolver()
                        .query(MovieContract.PopularMovieEntry.CONTENT_URI,
                                new String[]{MovieContract.PopularMovieEntry.TABLE_NAME +"." +"_id",
                                        MovieContract.PopularMovieEntry.TABLE_NAME +"." +
                                        MovieContract.PopularMovieEntry.COLUMN_MOVIE_ID,
                                        MovieContract.MovieDetailsEntry.COLUMN_POSTER_URL},
                                null, null, null);
                break;
            case GRID_TYPE_RATING:
                moviesCursor = mContext.getContentResolver()
                        .query(MovieContract.RatingMovieEntry.CONTENT_URI,
                                new String[]{MovieContract.RatingMovieEntry.TABLE_NAME +"." +"_id",
                                        MovieContract.RatingMovieEntry.TABLE_NAME +"." +
                                        MovieContract.RatingMovieEntry.COLUMN_MOVIE_ID,
                                        MovieContract.MovieDetailsEntry.COLUMN_POSTER_URL},
                                null, null, null);
                break;
            case GRID_TYPE_FAVORITE:
                moviesCursor = mContext.getContentResolver()
                        .query(MovieContract.FavoriteMovieEntry.CONTENT_URI,
                                new String[]{MovieContract.FavoriteMovieEntry.TABLE_NAME +"." +"_id",
                                        MovieContract.FavoriteMovieEntry.TABLE_NAME +"." +
                                        MovieContract.FavoriteMovieEntry.COLUMN_MOVIE_ID,
                                        MovieContract.MovieDetailsEntry.COLUMN_POSTER_URL},
                                null, null, null);
                break;

            default:
                Log.e(LOG_TAG, "Invalid type in loadDataFromDatabase()");
                return 0;
        }
        Log.i(LOG_TAG, "Done Loading data from database.");
        Log.v(LOG_TAG, "Number of Movies in Adaptor: " +moviesCursor.getCount());
        MovieGridFragment.this.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mMovieGridAdapter.changeCursor(moviesCursor);            }
        });
        return moviesCursor.getCount();
    }

    public boolean isMoreItemsNeeded(int lastVisibleItem) {
        if(mMovieGridAdapter.getCount() - lastVisibleItem < MOVIEGRID_THRESHOLD)
            return true;
        else
            return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movies_grid_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
               switch (item.getItemId()) {
//            case R.id.action_settings:
//                Snackbar.make(findViewById(R.id.mMoviesGridView), "This should open the settings", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//                break;

            case R.id.action_sort_order:

                //create and display a dialog to choose the sort order
                AlertDialog.Builder chooseSortOrderBuilder = new AlertDialog.Builder(mContext);
                CharSequence[] sortOrderChoices = GRID_TYPES;
                int currentChoice;
                switch (getMovieGrid_type()) {
                    case GRID_TYPE_POPULAR:
                        currentChoice = 0;
                        break;
                    case GRID_TYPE_RATING:
                        currentChoice = 1;
                        break;
                    case GRID_TYPE_FAVORITE:
                        currentChoice = 2;
                        break;
                    default:
                        currentChoice = 0;
                }
                chooseSortOrderBuilder.setTitle("Select Sort Order");
                chooseSortOrderBuilder.setSingleChoiceItems(sortOrderChoices, currentChoice,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                switch (which) {
                                    case 0:
                                        Log.v(LOG_TAG, "Selected order "+GRID_TYPE_POPULAR);
                                        if (getMovieGrid_type().equalsIgnoreCase(GRID_TYPE_POPULAR)) {
                                            Snackbar.make(mRootView.findViewById(R.id.imagesGridView),
                                                    "You are already in the most-popular view.",
                                                    Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                                            return;
                                        }
                                        setMovieGrid_type(GRID_TYPE_POPULAR);
                                        break;
                                    case 1:
                                        Log.v(LOG_TAG, "Selected order "+GRID_TYPE_RATING);
                                        if (getMovieGrid_type().equalsIgnoreCase(GRID_TYPE_RATING)) {
                                            Snackbar.make(mRootView.findViewById(R.id.imagesGridView),
                                                    "You are already in the highest-rated view.",
                                                    Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                                            return;
                                        }
                                        setMovieGrid_type(GRID_TYPE_RATING);
                                        break;
                                    case 2:
                                        Log.v(LOG_TAG, "Selected order "+GRID_TYPE_FAVORITE);
                                        if (getMovieGrid_type().equalsIgnoreCase(GRID_TYPE_FAVORITE)) {
                                            Snackbar.make(mRootView.findViewById(R.id.imagesGridView),
                                                    "You are already in the favorites view.",
                                                    Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                                            return;
                                        }
                                        setMovieGrid_type(GRID_TYPE_FAVORITE);
                                        break;
                                    default:
                                        Log.wtf(LOG_TAG, "An impossible grid type has been selected.");
                                }
                                OnRefresh();
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


    ContentValues[] fetchMovies(int startId) throws UnableToFetchData {
        ContentValues[] moviesCV;
        switch (movieGrid_type) {
            case GRID_TYPE_POPULAR:
                moviesCV = mMovieDbHelper.getMovies(movieGrid_type, Integer.toString(movieGrid_nextPageNum),
                        startId, null);
                break;
            case GRID_TYPE_RATING:
                Bundle extraParameters = new Bundle();
                extraParameters.putString("vote_count.gte", "100");
                moviesCV = mMovieDbHelper.getMovies(movieGrid_type, Integer.toString(movieGrid_nextPageNum),
                        startId, extraParameters);
                break;
            default:
                throw new UnableToFetchData("Invalid Type in fetchMovies()");
        }
        Log.v(LOG_TAG, "Fetched Movies in " + movieGrid_type + " order from page " + movieGrid_nextPageNum);
        movieGrid_nextPageNum += 1;
        return moviesCV;
    }

    void clearDataFromDatabase(String dataType){
        switch(dataType){
            case GRID_TYPE_POPULAR:
                mContext.getContentResolver().delete(MovieContract.PopularMovieEntry.CONTENT_URI, null, null);
                break;
            case GRID_TYPE_RATING:
                mContext.getContentResolver().delete(MovieContract.RatingMovieEntry.CONTENT_URI, null, null);
                break;
            case GRID_TYPE_FAVORITE:
                mContext.getContentResolver().delete(MovieContract.FavoriteMovieEntry.CONTENT_URI, null, null);
                break;
            default:
                Log.e(LOG_TAG, "Invalid Type in clearDataFromDatabase()");
        }
    }

    void insertIntoDb(String dataType, ContentValues[] data) {

        new Utility.InsertIntoCpTask().execute(dataType, data, mContext);

    }

    void addMoviesToAdapter(ContentValues[] newData) {
        Log.v(LOG_TAG, "Added page " + (movieGrid_nextPageNum-1) + " of movies to the list.");
        //Log.v(LOG_TAG, "NewCursor Data: " + DatabaseUtils.dumpCursorToString(newCursor));

        Cursor newCursor = Utility.parseMoviesCvToCursor(newData);
        Cursor currentCursor = mMovieGridAdapter.getCursor();
        if(currentCursor != null){
            MergeCursor mergedCursor = new MergeCursor(new Cursor[]{currentCursor, newCursor});
//           Log.v(LOG_TAG, "Number of Movies in Adaptor: " +mergedCursor.getCount());
            //Log.v(LOG_TAG, "MergedCursor Data: " +DatabaseUtils.dumpCursorToString(mergedCursor));
            mMovieGridAdapter.changeCursor(mergedCursor);
            Log.v(LOG_TAG, "Number of Movies in grid: " +mMovieGridAdapter.getCount());
        }
        else{
            mMovieGridAdapter.changeCursor(newCursor);
            Log.v(LOG_TAG, "Number of Movies in grid: " +mMovieGridAdapter.getCount());
        }

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // NO-OP
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        int lastVisibleItem = firstVisibleItem + visibleItemCount;
//        Log.v(LOG_TAG, "Last Visible Item: " + lastVisibleItem);
//        Log.v(LOG_TAG, "No. of items in dataset: " + mMovieGridAdapter.getCount());
        //add new movies if required
        if (isOnline && !getMovieGrid_type().equalsIgnoreCase(GRID_TYPE_FAVORITE)
                 && isMoreItemsNeeded(lastVisibleItem) && !isAdditionInProgress) {
            addMoviesToGrid();
        }
    }

    public void addMoviesToGrid() {
        isAdditionInProgress = true;
        // do this in a new thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final ContentValues[] moviesCv = fetchMovies(mMovieGridAdapter.getCount()+1);
                    Log.v(LOG_TAG, "Number of movies fetched: " +moviesCv.length);
                    //Log.v(LOG_TAG, "Data: " +DatabaseUtils.dumpCursorToString(moviesCursor));
                    // insert into the database asynchronously
                    insertIntoDb(movieGrid_type, moviesCv);
                    // add the movies to the adapter
                    MovieGridFragment.this.getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            addMoviesToAdapter(moviesCv);
                        }
                    });
                    isOnline = true;
                } catch (UnableToFetchData e) {
                    Log.e(LOG_TAG, "Unable to fetch data.");
                    isOnline = false;
                    Snackbar.make(mRootView.findViewById(R.id.imagesGridView),
                            getString(R.string.no_internet) +" " +getString(R.string.refresh_direction),
                            Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
                isAdditionInProgress = false;
            }
        }).start();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Cursor movieCursor = (Cursor)mMovieGridAdapter.getItem(position);

        // make a movie details bundle
        Bundle movieDetailsBundle = new Bundle();
        movieDetailsBundle.putInt("movie_id", movieCursor.getInt(movieCursor.getColumnIndex(
                MovieContract.MovieDetailsEntry.COLUMN_MOVIE_ID)));
        // call the onItemSelected of the containing activity
        ((MovieGridFragment.Callback) mContext).onItemSelected(movieDetailsBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        OnRefresh();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext = null;
        mMovieDbHelper = null;
    }
}
