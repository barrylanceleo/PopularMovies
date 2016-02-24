package com.barrylanceleo.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by barry on 2/22/16.
 */
public class TheMovieDbApiHelper {

    static final String TAG = TheMovieDbApiHelper.class.getSimpleName();

    Context mContext;
    String apiKey;

    public TheMovieDbApiHelper(Context mContext) {
        this.mContext = mContext;
        apiKey = mContext.getString(R.string.api_key);
    }

    List<Movie> parseJsonToMovies(JSONObject moviesJson) {
        //create a array list to hold all the movies
        List<Movie> movies = new ArrayList<>();

        try {
            JSONArray resultsArray = moviesJson.getJSONArray("results");
            Log.v(TAG, "Got " + resultsArray.length() + " movies from page " + moviesJson.getInt("page"));

            for (int i = 0; i < resultsArray.length(); i++) {
                // create a new movie object for each result
                Movie aMovie = new Movie();

                JSONObject result = resultsArray.getJSONObject(i);

                // process the poster url
                aMovie.setPosterPath(result.getString("poster_path"));
                String posterPath = aMovie.getPosterPath();
                Uri.Builder uriBuilder = new Uri.Builder();
                uriBuilder.scheme("http")
                        .authority("image.tmdb.org")
                        .appendPath("t")
                        .appendPath("p")
                        .appendPath("w342")
                        .appendPath(posterPath.substring(1, posterPath.length()));
                aMovie.setPosterUrl(uriBuilder.build().toString());
                Log.v(TAG, "Poster Url: " + aMovie.getPosterUrl());

                // process the backdrop url
                aMovie.setBackdropPath(result.getString("backdrop_path"));
                String backdropPath = aMovie.getBackdropPath();
                uriBuilder = new Uri.Builder();
                uriBuilder.scheme("http")
                        .authority("image.tmdb.org")
                        .appendPath("t")
                        .appendPath("p")
                        .appendPath("w780")
                        .appendPath(backdropPath.substring(1, backdropPath.length()));
                aMovie.setBackdropUrl(uriBuilder.build().toString());
                Log.v(TAG, "Backdrop Url: " + aMovie.getBackdropUrl());

                // other details
                aMovie.setAdult(result.getBoolean("adult"));
                aMovie.setOverview(result.getString("overview"));
                aMovie.setRelease_date(result.getString("release_date"));
                aMovie.setId(result.getInt("id"));
                aMovie.setTitle(result.getString("title"));
                aMovie.setLanguage(result.getString("original_language"));
                aMovie.setPopularity(result.getDouble("popularity"));
                aMovie.setVote_count(result.getInt("vote_count"));
                Log.v(TAG, "Vote Count: " + aMovie.getVote_count());
                aMovie.setVideo(result.getBoolean("video"));
                aMovie.setVote_average(result.getDouble("vote_average"));

                // get the genre ids
                JSONArray genreArray = result.getJSONArray("genre_ids");
                ArrayList<Integer> genres = new ArrayList<>();
                for (int j = 0; j < genreArray.length(); j++)
                    genres.add(genreArray.getInt(j));
                aMovie.setGenreIds(genres);

                // add to the list
                movies.add(aMovie);
            }

        } catch (JSONException e) {
            Log.e(TAG, "unable to find required tags in JSON response");
            e.printStackTrace();
        }

        for (Movie m : movies) {
            Log.v(TAG, m.getTitle());
            Log.v(TAG, m.getPosterPath());
        }

        return movies;
    }

    List<Movie> getMovies(String sortBy, String pageNumber) {
        return getMovies(sortBy, pageNumber, null);
    }

    List<Movie> getMovies(String sortBy, String pageNumber, Bundle extraParameters) {

        // build the URL to query
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme("http")
                .authority("api.themoviedb.org")
                .appendPath("3")
                .appendPath("discover")
                .appendPath("movie")
                .appendQueryParameter("sort_by", sortBy)
                .appendQueryParameter("page", pageNumber)
                .appendQueryParameter("api_key", this.apiKey);
        // .fragment("section-name"); // to add #section-name to the url

        if (extraParameters != null && !extraParameters.isEmpty()) {
            for (String key : extraParameters.keySet()) {
                uriBuilder.appendQueryParameter(key, extraParameters.getString(key));
            }
        }

        String requestString = uriBuilder.build().toString();
        Log.v(TAG, "Movie query URL: " + requestString);

        //query the URL
        JSONObject moviesJson = null;
        try {
            Log.v(TAG, "Starting background URL query.");
            AsyncTask queryTask = new queryTask().execute(requestString);
            Log.v(TAG, "Back from background URL query.");
            moviesJson = (JSONObject) queryTask.get();
            Log.v(TAG, "got the response from background URL query.");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return parseJsonToMovies(moviesJson);

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
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } finally {
                assert urlConnection != null;
                urlConnection.disconnect();
            }

            return moviesJson;
        }
    }
}
