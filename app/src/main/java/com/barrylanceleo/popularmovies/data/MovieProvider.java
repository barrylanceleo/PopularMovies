package com.barrylanceleo.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

public class MovieProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;

    static final int MOVIE_DETAILS = 100;
    static final int MOVIE_DETAILS_WITH_ID = 101;
    static final int POPULAR_MOVIES = 200;
    static final int RATING_MOVIES = 300;
    static final int FAVORITE_MOVIES = 400;
    static final int FAVORITE_MOVIE_WITH_ID = 401;

    // query builders to get the details of popular, rating and favorite movie_ids
    private static final SQLiteQueryBuilder sPopularMovieDetailsQueryBuilder;
    private static final SQLiteQueryBuilder sRatingMovieDetailsQueryBuilder;
    private static final SQLiteQueryBuilder sFavoriteMovieDetailsQueryBuilder;


    static{
        sPopularMovieDetailsQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //popular_movies INNER JOIN movie_details ON popular_movies.movie_id = movie_details.movie_id
        sPopularMovieDetailsQueryBuilder.setTables(
                MovieContract.PopularMovieEntry.TABLE_NAME + " INNER JOIN " +
                        MovieContract.MovieDetailsEntry.TABLE_NAME +
                        " ON " + MovieContract.PopularMovieEntry.TABLE_NAME +
                        "." + MovieContract.PopularMovieEntry.COLUMN_MOVIE_ID +
                        " = " + MovieContract.MovieDetailsEntry.TABLE_NAME +
                        "." + MovieContract.MovieDetailsEntry.COLUMN_MOVIE_ID);
    }

    static{
        sRatingMovieDetailsQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //popular_movies INNER JOIN movie_details ON popular_movies.movie_id = movie_details.movie_id
        sRatingMovieDetailsQueryBuilder.setTables(
                MovieContract.RatingMovieEntry.TABLE_NAME + " INNER JOIN " +
                        MovieContract.MovieDetailsEntry.TABLE_NAME +
                        " ON " + MovieContract.RatingMovieEntry.TABLE_NAME +
                        "." + MovieContract.RatingMovieEntry.COLUMN_MOVIE_ID +
                        " = " + MovieContract.MovieDetailsEntry.TABLE_NAME +
                        "." + MovieContract.MovieDetailsEntry.COLUMN_MOVIE_ID);
    }

    static{
        sFavoriteMovieDetailsQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //popular_movies INNER JOIN movie_details ON popular_movies.movie_id = movie_details.movie_id
        sFavoriteMovieDetailsQueryBuilder.setTables(
                MovieContract.FavoriteMovieEntry.TABLE_NAME + " INNER JOIN " +
                        MovieContract.MovieDetailsEntry.TABLE_NAME +
                        " ON " + MovieContract.FavoriteMovieEntry.TABLE_NAME +
                        "." + MovieContract.FavoriteMovieEntry.COLUMN_MOVIE_ID +
                        " = " + MovieContract.MovieDetailsEntry.TABLE_NAME +
                        "." + MovieContract.MovieDetailsEntry.COLUMN_MOVIE_ID);
    }


    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, MovieContract.PATH_DETAILS, MOVIE_DETAILS);
        matcher.addURI(authority, MovieContract.PATH_DETAILS +"/#", MOVIE_DETAILS_WITH_ID);

        matcher.addURI(authority, MovieContract.PATH_POPULAR, POPULAR_MOVIES);
        matcher.addURI(authority, MovieContract.PATH_RATING, RATING_MOVIES);
        matcher.addURI(authority, MovieContract.PATH_FAVORITE, FAVORITE_MOVIES);
        matcher.addURI(authority, MovieContract.PATH_FAVORITE +"/#", FAVORITE_MOVIE_WITH_ID);

        return matcher;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {

            case MOVIE_DETAILS:
                return MovieContract.MovieDetailsEntry.CONTENT_TYPE;
            case MOVIE_DETAILS_WITH_ID:
                return MovieContract.MovieDetailsEntry.CONTENT_ITEM_TYPE;
            case POPULAR_MOVIES:
                return MovieContract.PopularMovieEntry.CONTENT_TYPE;
            case RATING_MOVIES:
                return MovieContract.RatingMovieEntry.CONTENT_TYPE;
            case FAVORITE_MOVIES:
                return MovieContract.FavoriteMovieEntry.CONTENT_TYPE;
            case FAVORITE_MOVIE_WITH_ID:
                return MovieContract.FavoriteMovieEntry.CONTENT_ITEM_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor returnCursor;

        switch (sUriMatcher.match(uri)){
            case MOVIE_DETAILS:
                returnCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieDetailsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case MOVIE_DETAILS_WITH_ID: {
                String movie_id = uri.getLastPathSegment();
                returnCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieDetailsEntry.TABLE_NAME,
                        projection,
                        MovieContract.MovieDetailsEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[]{movie_id},
                        null,
                        null,
                        sortOrder
                );
            }
                break;
            case POPULAR_MOVIES:
                returnCursor = sPopularMovieDetailsQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case RATING_MOVIES:
                returnCursor = sRatingMovieDetailsQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case FAVORITE_MOVIES:
                returnCursor = sFavoriteMovieDetailsQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case FAVORITE_MOVIE_WITH_ID: {
                String movie_id = uri.getLastPathSegment();
                returnCursor = sFavoriteMovieDetailsQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        MovieContract.FavoriteMovieEntry.TABLE_NAME +"."
                                +MovieContract.FavoriteMovieEntry.COLUMN_MOVIE_ID +" = ?",
                        new String[]{movie_id},
                        null,
                        null,
                        sortOrder
                );
            }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }


        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return returnCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;
        long _id;
        switch (sUriMatcher.match(uri)) {
            case MOVIE_DETAILS:
                _id = db.insertWithOnConflict(MovieContract.MovieDetailsEntry.TABLE_NAME, null,
                        values, SQLiteDatabase.CONFLICT_REPLACE);
                if ( _id > 0 )
                    returnUri = MovieContract.MovieDetailsEntry.buildMovieDetailsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;

            case POPULAR_MOVIES:
                _id = db.insertWithOnConflict(MovieContract.PopularMovieEntry.TABLE_NAME, null,
                        values, SQLiteDatabase.CONFLICT_REPLACE);
                if ( _id > 0 )
                    // return the Uri for the details table
                    returnUri = MovieContract.MovieDetailsEntry.buildMovieDetailsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;

            case RATING_MOVIES:
                _id = db.insertWithOnConflict(MovieContract.RatingMovieEntry.TABLE_NAME, null,
                        values, SQLiteDatabase.CONFLICT_REPLACE);
                if ( _id > 0 )
                    // return the Uri for the details table
                    returnUri = MovieContract.MovieDetailsEntry.buildMovieDetailsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;

            case FAVORITE_MOVIES:
                _id = db.insertWithOnConflict(MovieContract.FavoriteMovieEntry.TABLE_NAME, null,
                        values, SQLiteDatabase.CONFLICT_REPLACE);
                if ( _id > 0 )
                    // return the Uri for the details table
                    returnUri = MovieContract.MovieDetailsEntry.buildMovieDetailsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long _id;
        int returnCount;
        switch (sUriMatcher.match(uri)) {
            case MOVIE_DETAILS:
                db.beginTransaction();
                returnCount = 0;
                for (ContentValues value : values){
                    _id = db.insertWithOnConflict(MovieContract.MovieDetailsEntry.TABLE_NAME, null,
                            value, SQLiteDatabase.CONFLICT_REPLACE);
                    if ( _id != -1)
                        returnCount++;
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                break;

            case POPULAR_MOVIES:
                db.beginTransaction();
                returnCount = 0;
                for (ContentValues value : values){
                    _id = db.insertWithOnConflict(MovieContract.PopularMovieEntry.TABLE_NAME, null,
                            value, SQLiteDatabase.CONFLICT_REPLACE);
                    if ( _id != -1)
                        returnCount++;
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                break;

            case RATING_MOVIES:
                db.beginTransaction();
                returnCount = 0;
                for (ContentValues value : values){
                    _id = db.insertWithOnConflict(MovieContract.RatingMovieEntry.TABLE_NAME, null,
                            value, SQLiteDatabase.CONFLICT_REPLACE);
                    if ( _id != -1)
                        returnCount++;
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                break;

            case FAVORITE_MOVIES:
                db.beginTransaction();
                returnCount = 0;
                for (ContentValues value : values){
                    _id = db.insertWithOnConflict(MovieContract.FavoriteMovieEntry.TABLE_NAME, null,
                            value, SQLiteDatabase.CONFLICT_REPLACE);
                    if ( _id != -1)
                        returnCount++;
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                break;

            default:
                return super.bulkInsert(uri, values);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsDeleted;
        switch (sUriMatcher.match(uri)) {
            case MOVIE_DETAILS:
                rowsDeleted = db.delete(
                        MovieContract.MovieDetailsEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case POPULAR_MOVIES:
                rowsDeleted = db.delete(
                        MovieContract.PopularMovieEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case RATING_MOVIES:
                rowsDeleted = db.delete(
                    MovieContract.RatingMovieEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case FAVORITE_MOVIES:
                rowsDeleted = db.delete(
                        MovieContract.FavoriteMovieEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsUpdated;
        switch (sUriMatcher.match(uri)) {
            case MOVIE_DETAILS:
                rowsUpdated = db.update(
                        MovieContract.MovieDetailsEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            case POPULAR_MOVIES:
                rowsUpdated = db.update(
                        MovieContract.PopularMovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            case RATING_MOVIES:
                rowsUpdated = db.update(
                        MovieContract.RatingMovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            case FAVORITE_MOVIES:
                rowsUpdated = db.update(
                        MovieContract.FavoriteMovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}
