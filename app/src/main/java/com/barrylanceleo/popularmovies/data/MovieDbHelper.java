package com.barrylanceleo.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.barrylanceleo.popularmovies.data.MovieContract.FavoriteMovieEntry;
import com.barrylanceleo.popularmovies.data.MovieContract.MovieDetailsEntry;
import com.barrylanceleo.popularmovies.data.MovieContract.PopularMovieEntry;
import com.barrylanceleo.popularmovies.data.MovieContract.RatingMovieEntry;

public class MovieDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "movie.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create tables to hold the popular, recent and favorite movies list and another table to hold the movie details

        final String SQL_CREATE_MOVIE_DETAILS_TABLE = "CREATE TABLE " + MovieDetailsEntry.TABLE_NAME + " (" +
                MovieDetailsEntry.COLUMN_MOVIE_ID + " INTEGER PRIMARY KEY, " +
                MovieDetailsEntry.COLUMN_TITLE + " TEXT, " +
                MovieDetailsEntry.COLUMN_POSTER_PATH + " TEXT, " +
                MovieDetailsEntry.COLUMN_POSTER_URL + " TEXT, " +
                MovieDetailsEntry.COLUMN_ADULT + " INTEGER, " +
                MovieDetailsEntry.COLUMN_OVERVIEW + " TEXT, " +
                MovieDetailsEntry.COLUMN_RELEASE_DATE + " TEXT, " +
                MovieDetailsEntry.COLUMN_GENRE_IDS + " TEXT, " +
                MovieDetailsEntry.COLUMN_LANGUAGE + " TEXT, " +
                MovieDetailsEntry.COLUMN_BACKDROP_PATH + " TEXT, " +
                MovieDetailsEntry.COLUMN_BACKDROP_URL + " TEXT, " +
                MovieDetailsEntry.COLUMN_POPULARITY + " REAL, " +
                MovieDetailsEntry.COLUMN_VOTE_COUNT + " INTEGER, " +
                MovieDetailsEntry.COLUMN_VOTE_AVERAGE + " REAL " +
                " );";

        final String SQL_CREATE_POPULAR_MOVIES_TABLE = "CREATE TABLE " + PopularMovieEntry.TABLE_NAME + " (" +
                PopularMovieEntry._ID + " INTEGER PRIMARY KEY," +
                PopularMovieEntry.COLUMN_MOVIE_ID + " INTEGER UNIQUE NOT NULL, " +
                " FOREIGN KEY(" + PopularMovieEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieDetailsEntry.TABLE_NAME + "(" + MovieDetailsEntry.COLUMN_MOVIE_ID + ")" +
                " );";

        final String SQL_CREATE_RECENT_MOVIES_TABLE = "CREATE TABLE " + RatingMovieEntry.TABLE_NAME + " (" +
                RatingMovieEntry._ID + " INTEGER PRIMARY KEY," +
                RatingMovieEntry.COLUMN_MOVIE_ID + " INTEGER UNIQUE NOT NULL, " +
                " FOREIGN KEY(" + RatingMovieEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieDetailsEntry.TABLE_NAME + "(" + MovieDetailsEntry.COLUMN_MOVIE_ID + ")" +
                " );";

        final String SQL_CREATE_FAVORITE_MOVIES_TABLE = "CREATE TABLE " + FavoriteMovieEntry.TABLE_NAME + " (" +
                FavoriteMovieEntry._ID + " INTEGER PRIMARY KEY," +
                FavoriteMovieEntry.COLUMN_MOVIE_ID + " INTEGER UNIQUE NOT NULL, " +
                " FOREIGN KEY(" + FavoriteMovieEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieDetailsEntry.TABLE_NAME + "(" + MovieDetailsEntry.COLUMN_MOVIE_ID + ")" +
                " );";

        db.execSQL(SQL_CREATE_MOVIE_DETAILS_TABLE);
        db.execSQL(SQL_CREATE_POPULAR_MOVIES_TABLE);
        db.execSQL(SQL_CREATE_RECENT_MOVIES_TABLE);
        db.execSQL(SQL_CREATE_FAVORITE_MOVIES_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over

        db.execSQL("DROP TABLE IF EXISTS " + MovieDetailsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PopularMovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RatingMovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FavoriteMovieEntry.TABLE_NAME);
        onCreate(db);

    }
}
