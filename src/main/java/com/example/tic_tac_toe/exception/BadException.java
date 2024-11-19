package com.example.tic_tac_toe.exception;

public class BadException extends Exception {
    private String message;

    public BadException(String message) {
        super(message);
    }
}
