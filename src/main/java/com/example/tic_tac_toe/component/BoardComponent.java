package com.example.tic_tac_toe.component;

import com.example.tic_tac_toe.model.entity.Board;
import com.example.tic_tac_toe.model.entity.Cell;
import com.example.tic_tac_toe.model.entity.Player;
import com.example.tic_tac_toe.repository.BoardRepository;
import com.example.tic_tac_toe.repository.CellRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;

import static com.example.tic_tac_toe.util.CacheConstant.BOARD_ID_TO_BOARD;
import static com.example.tic_tac_toe.util.CacheConstant.USERNAME_TO_BOARD_ID;


@Component
@RequiredArgsConstructor
public class BoardComponent {
    private final BoardRepository boardRepository;
    private final CellRepository cellRepository;
    private final PlayerComponent playerComponent;
    private final CaffeineCacheComponent caffeineCacheComponent;

    public Board addPlayerToBoard(Player player) {
        Optional<Board> activeBoardByPlayerId = boardRepository.findActiveBoardByPlayerId(player.getId());

        if (activeBoardByPlayerId.isPresent()) {
            return activeBoardByPlayerId.get();
        }

        Board board = boardRepository.findActiveBoardWithLessThanTwoUsers()
                .orElseGet(this::createTicTacToeBoard);
        addPlayerToBoard(player, board);
        addBoardToPlayer(board, player);

        return updateEntities(board, player);
    }

    private Board updateEntities(Board board, Player player) {
        Board saved = save(board);
        playerComponent.save(player);
        cellRepository.saveAll(board.getCells());
        caffeineCacheComponent.put(USERNAME_TO_BOARD_ID, player.getUserName(), board.getId());

        return saved;
    }

    public void closeGame(Board board) {
        board.setActive(false);
        boardRepository.save(board);
    }

    public Board save(Board board) {
        Board saved = boardRepository.save(board);
        caffeineCacheComponent.put(BOARD_ID_TO_BOARD, board.getId().toString(), board);

        return saved;
    }

    private void addBoardToPlayer(Board board, Player player) {
        if (player.getBoards() == null) {
            player.setBoards(new HashSet<>());
        }

        player.getBoards().add(board);
    }

    private void addPlayerToBoard(Player player, Board board) {
        if (board.getPlayers() == null) {
            board.setPlayers(new HashSet<>());
        }
        board.getPlayers().add(player);
    }

    private Board createTicTacToeBoard() {
        Board board = Board.builder()
                .rows(3)
                .columns(3)
                .build();
        int amountOfBoxes = board.getRows() * board.getColumns();
        for (int i = 0; i < amountOfBoxes; i++) {
            Cell cell = Cell.builder()
                    .index(i + 1)
                    .build();
            cell.setBoard(board);
            board.getCells().add(cell);
        }

        return board;
    }
}
