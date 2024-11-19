package com.example.tic_tac_toe.component;

import com.example.tic_tac_toe.model.entity.Board;
import com.example.tic_tac_toe.model.entity.IndexBox;
import com.example.tic_tac_toe.model.entity.Player;
import com.example.tic_tac_toe.repository.BoardRepository;
import com.example.tic_tac_toe.repository.IndexBoxRepository;
import com.example.tic_tac_toe.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;


@Component
@RequiredArgsConstructor
public class BoardComponent {
    private final BoardRepository boardRepository;
    private final IndexBoxRepository indexBoxRepository;
    private final PlayerRepository playerRepository;

    public Board addPlayerToBoard(Player player) {
        Board board = boardRepository.findActiveBoardWithLessThanTwoUsers()
                .orElseGet(this::createTicTacToeBoard);

        if (board.getPlayers() == null) {
            board.setPlayers(new HashSet<>());
        }
        board.getPlayers().add(player);

        if (player.getBoards() == null) {
            player.setBoards(new HashSet<>());
        }
        player.getBoards().add(board);
        Board saved = boardRepository.save(board);
        playerRepository.save(player);
        indexBoxRepository.saveAll(board.getIndexBoxes());
        return saved;
    }

    private Board createTicTacToeBoard() {
        Board board = Board.builder().indexBoxes(new HashSet<>()).build();
        for (int i = 0; i < 9; i++) {
            IndexBox indexBox = IndexBox.builder()
                    .index(i + 1)
                    .build();
            indexBox.setBoard(board);
            board.getIndexBoxes().add(indexBox);
        }

        return board;
    }
}
