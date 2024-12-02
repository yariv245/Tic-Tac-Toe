package com.example.tic_tac_toe.exception;


public class BadRequestException extends RuntimeException {
    private String message;

    public BadRequestException(String message) {
        super(message);
    }
}
