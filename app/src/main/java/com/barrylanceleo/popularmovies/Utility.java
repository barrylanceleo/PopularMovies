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
import com.barrylanceleo.popularmovies.fragments.MovieGridFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Utility {

    private static final String LOG_TAG = Utility.class.getSimpleName();
    public static final String IMAGE_DIRECTORY = "Popular Movies";

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

        for (ContentValues cv : cvs) {

            MatrixCursor.RowBuilder movieRow = moviesCursor.newRow();

            // this order depends on the column order defined above
            movieRow.add(cv.get("_id"));
            movieRow.add(cv.get(MovieContract.MovieDetailsEntry.COLUMN_MOVIE_ID));
            movieRow.add(cv.get(MovieContract.MovieDetailsEntry.COLUMN_TITLE));
            movieRow.add(cv.get(MovieContract.MovieDetailsEntry.COLUMN_POSTER_PATH));
            movieRow.add(cv.get(MovieContract.MovieDetailsEntry.COLUMN_POSTER_URL));
            movieRow.add(cv.get(MovieContract.MovieDetailsEntry.COLUMN_ADULT));
            movieRow.add(cv.get(MovieContract.MovieDetailsEntry.COLUMN_OVERVIEW));
            movieRow.add(cv.get(MovieContract.MovieDetailsEntry.COLUMN_RELEASE_DATE));
            movieRow.add(cv.get(MovieContract.MovieDetailsEntry.COLUMN_GENRE_IDS));
            movieRow.add(cv.get(MovieContract.MovieDetailsEntry.COLUMN_LANGUAGE));
            movieRow.add(cv.get(MovieContract.MovieDetailsEntry.COLUMN_BACKDROP_PATH));
            movieRow.add(cv.get(MovieContract.MovieDetailsEntry.COLUMN_BACKDROP_URL));
            movieRow.add(cv.get(MovieContract.MovieDetailsEntry.COLUMN_POPULARITY));
            movieRow.add(cv.get(MovieContract.MovieDetailsEntry.COLUMN_VOTE_COUNT));
            movieRow.add(cv.get(MovieContract.MovieDetailsEntry.COLUMN_VOTE_AVERAGE));

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

    public static File downloadFile(String fileUrl, String filepath) throws MovieDbApiHelper.UnableToFetchDataException{
        HttpURLConnection urlConnection = null;
        try {
            Log.i(LOG_TAG, "Downloading " +fileUrl +" to " +filepath);
            // open the file to write to
            File destinationFile = new File(filepath);
            // create the file if it doesn't exist, if the parent doesn't exist return null
            if (!destinationFile.exists()) {
                if(!destinationFile.getParentFile().isDirectory()) {
                    Log.e(LOG_TAG, "Parent directory doesn't exist");
                    return null;
                }
                if(!destinationFile.createNewFile()) {
                    Log.e(LOG_TAG, "Unable to create new file");
                    return null;
                }
            }
            FileOutputStream fileOutputStream = new FileOutputStream(filepath);

            // open a http connection and fetch the file
            URL requestUrl = new URL(fileUrl);
            urlConnection = (HttpURLConnection) requestUrl.openConnection();
            BufferedInputStream urlInputStream = new BufferedInputStream(urlConnection.getInputStream());
            final int BUFFER_SIZE = 1024;
            byte buffer[] = new byte[BUFFER_SIZE];
            int read_size;
            while ((read_size = urlInputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
                fileOutputStream.write(buffer, 0, read_size);
            }
            return destinationFile;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Exception: No internet?", e);
            throw new MovieDbApiHelper.UnableToFetchDataException("No internet", e);
        } finally {
            if(urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}


