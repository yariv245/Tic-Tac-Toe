package com.example.tic_tac_toe.component;

import com.example.tic_tac_toe.model.entity.Board;
import com.example.tic_tac_toe.model.entity.Player;

public interface BoardComponent {
    Board addPlayerToBoard(Player player);

    void closeGame(Board board);

    Board save(Board board);
}
