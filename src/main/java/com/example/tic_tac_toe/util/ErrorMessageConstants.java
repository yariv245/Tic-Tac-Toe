package com.example.tic_tac_toe.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorMessageConstants {
    public final static String INDEX_ERROR_MESSAGE = "index must be 1-9";
    public final static String PLAY_MOVE_ERROR_MESSAGE = "index must be 1-9";
    public final static String HEADER_PARAM_NOT_FOUND = "%s not Found";
    public final static String WRONG_PASSWORD_MESSAGE = "Wrong password !";
    public final static String FIRST_PLAYER_NOT_FOUND_MESSAGE = "Couldn't find first player id";
    public final static String OPPONENT_NOT_FOUND_MESSAGE = "Couldn't find any Opponent to player: %s";
    public final static String CELL_ALREADY_TAKEN_MESSAGE = "Cell %d already taken!";
}
