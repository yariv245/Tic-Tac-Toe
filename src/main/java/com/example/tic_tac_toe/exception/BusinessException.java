package com.example.tic_tac_toe.exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BusinessException extends RuntimeException {
    private String message;
}
