package com.barrylanceleo.popularmovies;

import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.barrylanceleo.popularmovies.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public final class MovieDbApiHelper {

    public static final String TAG = MovieDbApiHelper.class.getSimpleName();
    public static final int movieCountPerPage = 20;
    public final String mApiKey;


    public MovieDbApiHelper(String apiKey){
        mApiKey = apiKey;
    }




    ContentValues[] parseJsonToCv(JSONObject moviesJson, int startId) {

        ContentValues [] moviesCv;
        try {
            JSONArray resultsArray = moviesJson.getJSONArray("results");
            Log.v(TAG, "Got " + resultsArray.length() + " movies from page " + moviesJson.getInt("page"));
            moviesCv = new ContentValues[resultsArray.length()];

            for (int i = 0; i < resultsArray.length(); i++) {

                JSONObject result = resultsArray.getJSONObject(i);
                moviesCv[i] = new ContentValues();

                // this order depends on the column order defined above
                moviesCv[i].put("_id", startId++);
                moviesCv[i].put(MovieContract.MovieDetailsEntry.COLUMN_MOVIE_ID, result.getInt("id"));
                moviesCv[i].put(MovieContract.MovieDetailsEntry.COLUMN_TITLE, result.getString("title"));

                String posterPath = result.getString("poster_path");
                moviesCv[i].put(MovieContract.MovieDetailsEntry.COLUMN_POSTER_PATH, posterPath);

                // add the poster URL
                Uri.Builder uriBuilder = new Uri.Builder();
                uriBuilder.scheme("http")
                        .authority("image.tmdb.org")
                        .appendPath("t")
                        .appendPath("p")
                        .appendPath("w342")
                        .appendPath(posterPath.substring(1, posterPath.length()));
                moviesCv[i].put(MovieContract.MovieDetailsEntry.COLUMN_POSTER_URL, uriBuilder.build().toString());

                moviesCv[i].put(MovieContract.MovieDetailsEntry.COLUMN_ADULT, result.getBoolean("adult"));
                moviesCv[i].put(MovieContract.MovieDetailsEntry.COLUMN_OVERVIEW, result.getString("overview"));
                moviesCv[i].put(MovieContract.MovieDetailsEntry.COLUMN_RELEASE_DATE, result.getString("release_date"));
                moviesCv[i].put(MovieContract.MovieDetailsEntry.COLUMN_GENRE_IDS, result.getJSONArray("genre_ids").toString());
                moviesCv[i].put(MovieContract.MovieDetailsEntry.COLUMN_LANGUAGE, result.getString("original_language"));

                String backdropPath = result.getString("backdrop_path");
                moviesCv[i].put(MovieContract.MovieDetailsEntry.COLUMN_BACKDROP_PATH, backdropPath);
                // add the backdrop URL
                uriBuilder = new Uri.Builder();
                uriBuilder.scheme("http")
                        .authority("image.tmdb.org")
                        .appendPath("t")
                        .appendPath("p")
                        .appendPath("w780")
                        .appendPath(backdropPath.substring(1, backdropPath.length()));
                moviesCv[i].put(MovieContract.MovieDetailsEntry.COLUMN_BACKDROP_URL, uriBuilder.build().toString());

                moviesCv[i].put(MovieContract.MovieDetailsEntry.COLUMN_POPULARITY, result.getDouble("popularity"));
                moviesCv[i].put(MovieContract.MovieDetailsEntry.COLUMN_VOTE_COUNT, result.getInt("vote_count"));
                moviesCv[i].put(MovieContract.MovieDetailsEntry.COLUMN_VOTE_AVERAGE, result.getDouble("vote_average"));
            }
        } catch (JSONException e) {
            Log.e(TAG, "unable to find required tags in JSON response");
            e.printStackTrace();
            return null;
        }

        Log.v(TAG, "In MovieDbHelper.parseJsonToCv().\nCount: " + moviesCv.length);
        return moviesCv;
    }

    ContentValues[] getMovies(String sortBy, String pageNumber, int startId, Bundle extraParameters) throws UnableToFetchData {

        // build the URL to query
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme("http")
                .authority("api.themoviedb.org")
                .appendPath("3")
                .appendPath("discover")
                .appendPath("movie")
                .appendQueryParameter("sort_by", sortBy)
                .appendQueryParameter("page", pageNumber)
                .appendQueryParameter("api_key", this.mApiKey);
        // .fragment("section-name"); // to add #section-name to the url

        if (extraParameters != null && !extraParameters.isEmpty()) {
            for (String key : extraParameters.keySet()) {
                uriBuilder.appendQueryParameter(key, extraParameters.getString(key));
            }
        }

        String requestString = uriBuilder.build().toString();

        //query the URL
        JSONObject moviesJson = null;
        try {
            Log.v(TAG, "Querying " + requestString);
            AsyncTask queryTask = new queryTask().execute(requestString);
            moviesJson = (JSONObject) queryTask.get();
        } catch (CancellationException e) {
            Log.v(TAG, "Task Cancelled");
            throw new UnableToFetchData("No internet", e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            Log.v(TAG, "Task Execution failed");
            throw new UnableToFetchData("No internet", e);
        }
        return parseJsonToCv(moviesJson, startId);

    }

    private class queryTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            JSONObject moviesJson = null;
            try {
                URL requestUrl = new URL(params[0]);
                urlConnection = (HttpURLConnection) requestUrl.openConnection();
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                moviesJson = new JSONObject(sb.toString());
                br.close();
            } catch (IOException e) {
                Log.v(TAG, "Exception: No internet?");
                cancel(true);
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                assert urlConnection != null;
                urlConnection.disconnect();
            }
            return moviesJson;
        }
    }
}
