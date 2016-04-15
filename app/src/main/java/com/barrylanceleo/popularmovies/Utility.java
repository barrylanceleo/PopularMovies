package com.barrylanceleo.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.barrylanceleo.popularmovies.data.MovieContract;

public class Utility {

    static final String LOG_TAG = Utility.class.getSimpleName();

    public static String getPreferredSortOrder(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sortOrder_key),
                context.getString(R.string.pref_sortOrder_default));
    }

    public static void setPreferredSortOrder(Context context, String sortOrder) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(context.getString(R.string.pref_sortOrder_key), sortOrder);
        editor.apply();
    }

    public static Cursor parseMoviesCvToCursor(ContentValues[] cvs){
        //create a cursor containing all movies
        MatrixCursor moviesCursor = new MatrixCursor(new String[]{
                "_id",
                MovieContract.MovieDetailsEntry.COLUMN_MOVIE_ID,
                MovieContract.MovieDetailsEntry.COLUMN_TITLE,
                MovieContract.MovieDetailsEntry.COLUMN_POSTER_PATH,
                MovieContract.MovieDetailsEntry.COLUMN_POSTER_URL,
                MovieContract.MovieDetailsEntry.COLUMN_ADULT,
                MovieContract.MovieDetailsEntry.COLUMN_OVERVIEW,
                MovieContract.MovieDetailsEntry.COLUMN_RELEASE_DATE,
                MovieContract.MovieDetailsEntry.COLUMN_GENRE_IDS,
                MovieContract.MovieDetailsEntry.COLUMN_LANGUAGE,
                MovieContract.MovieDetailsEntry.COLUMN_BACKDROP_PATH,
                MovieContract.MovieDetailsEntry.COLUMN_BACKDROP_URL,
                MovieContract.MovieDetailsEntry.COLUMN_POPULARITY,
                MovieContract.MovieDetailsEntry.COLUMN_VOTE_COUNT,
                MovieContract.MovieDetailsEntry.COLUMN_VOTE_AVERAGE }, 10);

        for(int i = 0; i < cvs.length; i++){

            MatrixCursor.RowBuilder movieRow = moviesCursor.newRow();

            // this order depends on the column order defined above
            movieRow.add(cvs[i].get("_id"));
            movieRow.add(cvs[i].get(MovieContract.MovieDetailsEntry.COLUMN_MOVIE_ID));
            movieRow.add(cvs[i].get(MovieContract.MovieDetailsEntry.COLUMN_TITLE));
            movieRow.add(cvs[i].get(MovieContract.MovieDetailsEntry.COLUMN_POSTER_PATH));
            movieRow.add(cvs[i].get(MovieContract.MovieDetailsEntry.COLUMN_POSTER_URL));
            movieRow.add(cvs[i].get(MovieContract.MovieDetailsEntry.COLUMN_ADULT));
            movieRow.add(cvs[i].get(MovieContract.MovieDetailsEntry.COLUMN_OVERVIEW));
            movieRow.add(cvs[i].get(MovieContract.MovieDetailsEntry.COLUMN_RELEASE_DATE));
            movieRow.add(cvs[i].get(MovieContract.MovieDetailsEntry.COLUMN_GENRE_IDS));
            movieRow.add(cvs[i].get(MovieContract.MovieDetailsEntry.COLUMN_LANGUAGE));
            movieRow.add(cvs[i].get(MovieContract.MovieDetailsEntry.COLUMN_BACKDROP_PATH));
            movieRow.add(cvs[i].get(MovieContract.MovieDetailsEntry.COLUMN_BACKDROP_URL));
            movieRow.add(cvs[i].get(MovieContract.MovieDetailsEntry.COLUMN_POPULARITY));
            movieRow.add(cvs[i].get(MovieContract.MovieDetailsEntry.COLUMN_VOTE_COUNT));
            movieRow.add(cvs[i].get(MovieContract.MovieDetailsEntry.COLUMN_VOTE_AVERAGE));

        }
        return moviesCursor;
    }

    public static class InsertIntoCpTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            Log.v(LOG_TAG, "Starting DB insert");
            String dataType = (String) params[0];
            ContentValues[] detailsCvArray = (ContentValues[]) params[1];
            Context context = (Context) params[2];
            int rowCount = detailsCvArray.length;
            if(rowCount == 0){
                Log.v(LOG_TAG, "Trying to insert a empty cursor.");
                return null;
            }
            Log.v(LOG_TAG, "Data to be inserted - " +rowCount +" rows.\n");
            ContentValues[] idCvArray = new ContentValues[rowCount];
            switch (dataType) {
                case MovieGridFragment.GRID_TYPE_POPULAR: {
                    for(int i = 0; i < rowCount; i++){
                        detailsCvArray[i].remove(MovieContract.PopularMovieEntry._ID);
                        int movie_id = detailsCvArray[i].getAsInteger(MovieContract.PopularMovieEntry.COLUMN_MOVIE_ID);
                        idCvArray[i] = new ContentValues();
                        idCvArray[i].put(MovieContract.PopularMovieEntry.COLUMN_MOVIE_ID, movie_id);
                    }
                    // insert into the popular table
                    context.getContentResolver().
                            bulkInsert(MovieContract.PopularMovieEntry.CONTENT_URI, idCvArray);

                    // insert into the details table
                    context.getContentResolver().
                            bulkInsert(MovieContract.MovieDetailsEntry.CONTENT_URI, detailsCvArray);

                    break;
                }
                case MovieGridFragment.GRID_TYPE_RATING: {
                    for(int i = 0; i < rowCount; i++){
                        detailsCvArray[i].remove(MovieContract.RatingMovieEntry._ID);
                        int movie_id = detailsCvArray[i].getAsInteger(MovieContract.RatingMovieEntry.COLUMN_MOVIE_ID);
                        idCvArray[i] = new ContentValues();
                        idCvArray[i].put(MovieContract.RatingMovieEntry.COLUMN_MOVIE_ID, movie_id);
                    }
                    // insert into the rating table
                    context.getContentResolver().
                            bulkInsert(MovieContract.RatingMovieEntry.CONTENT_URI, idCvArray);

                    // insert into the details table
                    context.getContentResolver().
                            bulkInsert(MovieContract.MovieDetailsEntry.CONTENT_URI, detailsCvArray);

                    break;
                }
                case MovieGridFragment.GRID_TYPE_FAVORITE: {
                    for(int i = 0; i < rowCount; i++){
                        detailsCvArray[i].remove(MovieContract.FavoriteMovieEntry._ID);
                        int movie_id = detailsCvArray[i].getAsInteger(MovieContract.FavoriteMovieEntry.COLUMN_MOVIE_ID);
                        idCvArray[i] = new ContentValues();
                        idCvArray[i].put(MovieContract.FavoriteMovieEntry.COLUMN_MOVIE_ID, movie_id);
                    }
                    // insert into the favorite table
                    context.getContentResolver().
                            bulkInsert(MovieContract.FavoriteMovieEntry.CONTENT_URI, idCvArray);

                    // insert into the details table
                    context.getContentResolver().
                            bulkInsert(MovieContract.MovieDetailsEntry.CONTENT_URI, detailsCvArray);

                    break;
                }

                default:
                    Log.e(LOG_TAG, "Trying to insert into Invalid table");
            }
            Log.v(LOG_TAG, "End of DB insert");
            return null;
        }
    }
}


