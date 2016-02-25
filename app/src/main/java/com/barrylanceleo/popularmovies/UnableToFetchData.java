package com.barrylanceleo.popularmovies;


public class UnableToFetchData extends Exception {

    public UnableToFetchData(String message) {
        super(message);
    }

    public UnableToFetchData(String message, Throwable throwable) {
        super(message, throwable);
    }
}
