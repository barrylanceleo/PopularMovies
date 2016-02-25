package com.barrylanceleo.popularmovies;


public class NoInternetException extends Exception {

    public NoInternetException(String message) {
        super(message);
    }

    public NoInternetException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
