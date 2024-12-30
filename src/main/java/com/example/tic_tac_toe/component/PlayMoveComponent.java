package com.example.tic_tac_toe.component;

import com.example.tic_tac_toe.model.PlayMove;
import com.example.tic_tac_toe.model.entity.Board;
import com.example.tic_tac_toe.model.entity.Cell;
import com.example.tic_tac_toe.model.entity.Player;
import com.example.tic_tac_toe.model.request.PlayRequest;

import java.util.Optional;

public interface PlayMoveComponent {
    Cell play(PlayRequest request, Board board, Player player);

    boolean isDraw(Board board, PlayMove playMove);

    boolean isWon(Cell cell, Board board);

    boolean isSamePlayer(Optional<Cell> indexBox, Optional<Cell> indexBox2, Optional<Cell> indexBox3);

    boolean isSameMove(Optional<Cell> indexBox, Optional<Cell> indexBox2, Optional<Cell> indexBox3);
}
