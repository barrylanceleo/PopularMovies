package com.barrylanceleo.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by barry on 4/10/16.
 */
public class MovieContract {

    public static final String CONTENT_AUTHORITY = "com.barrylanceleo.popularmovies.data";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // path to access corresponding data
    public static final String PATH_POPULAR = "popular";
    public static final String PATH_RATING = "rating";
    public static final String PATH_FAVORITE = "favorite";
    public static final String PATH_DETAILS = "details";

    // defines the popular movies table
    public static final class PopularMovieEntry implements BaseColumns{
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_POPULAR).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POPULAR;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POPULAR;

        // Table name
        public static final String TABLE_NAME = "popular";

        //columns
        public static final String COLUMN_MOVIE_ID = "movie_id";

    }

    // defines the recent movies table
    public static final class RatingMovieEntry implements BaseColumns{
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_RATING).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RATING;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RATING;

        // Table name
        public static final String TABLE_NAME = "recent";

        //columns
        public static final String COLUMN_MOVIE_ID = "movie_id";

    }

    // defines the favorite movies table
    public static final class FavoriteMovieEntry implements BaseColumns{
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITE;

        // Table name
        public static final String TABLE_NAME = "favorite";

        //columns
        public static final String COLUMN_MOVIE_ID = "movie_id";

    }

    // defines the movie details table
    public static final class MovieDetailsEntry implements BaseColumns{
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DETAILS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DETAILS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DETAILS;

        // Table name
        public static final String TABLE_NAME = "details";

        //columns
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_POSTER_URL = "poster_url";
        public static final String COLUMN_ADULT = "adult";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_GENRE_IDS = "genre_ids";
        public static final String COLUMN_LANGUAGE = "language";
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";
        public static final String COLUMN_BACKDROP_URL = "backdrop_url";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_VOTE_COUNT = "vote_count";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";

        public static Uri buildMovieDetailsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }
}
