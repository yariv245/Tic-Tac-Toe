package com.example.tic_tac_toe.model;

import java.util.Arrays;
import java.util.Optional;

public enum PlayMove {
    X,O;

    public static Optional<PlayMove> findOpponentPlayMove(PlayMove playMove) {
        return Arrays.stream(PlayMove.values())
                .filter(pm -> !pm.equals(playMove))
                .findFirst();
    }
}
