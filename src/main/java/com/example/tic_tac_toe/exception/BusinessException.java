package com.example.tic_tac_toe.exception;

public class BusinessException extends RuntimeException {
    private String message;

    public BusinessException(String message) {
        super(message);
    }
}
