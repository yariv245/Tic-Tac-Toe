package com.example.tic_tac_toe.exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BadRequestException extends RuntimeException {
    private String message;
}
