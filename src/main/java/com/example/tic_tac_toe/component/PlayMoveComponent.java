package com.example.tic_tac_toe.component;

import com.example.tic_tac_toe.exception.BadException;
import com.example.tic_tac_toe.model.PlayMove;
import com.example.tic_tac_toe.model.entity.Board;
import com.example.tic_tac_toe.model.entity.Cell;
import com.example.tic_tac_toe.model.entity.Player;
import com.example.tic_tac_toe.model.request.PlayRequest;
import com.example.tic_tac_toe.repository.BoardRepository;
import com.example.tic_tac_toe.repository.CellRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class PlayMoveComponent {
    private final BoardRepository boardRepository;
    private final CellRepository cellRepository;

    public boolean play(PlayRequest request, Board board, Player player) throws BadException {
        Cell cell = getCell(request.getIndex(), board);

        if (cell.getPlayMove() != null || cell.getPlayer() != null)
            throw new BadException("Index box already taken!");

        cell.setPlayMove(request.getPlayMove());
        cell.setPlayer(player);
        cellRepository.save(cell);

        return isWon(cell, board);
    }

    public void closeGame(Board board) {
        board.setActive(false);
        boardRepository.save(board);
    }

    private boolean isWon(Cell cell, Board board) {
        Map<Integer, Cell> indexNumberToEntityMap = getIndexNumberToEntityMap(board);
        boolean checkedHorizontal = checkHorizontal(indexNumberToEntityMap, cell, board);
        boolean checkedVertical = checkVertical(indexNumberToEntityMap, cell, board);
        boolean checkedDiagonal = checkDiagonal(indexNumberToEntityMap, cell,board);

        return checkedHorizontal || checkedVertical || checkedDiagonal;
    }

    private Map<Integer, Cell> getIndexNumberToEntityMap(Board board) {
        return board.getCells()
                .stream()
                .collect(Collectors.toMap(Cell::getIndex, Function.identity()));
    }

    private Cell getCell(int index, Board board) {
        return board.getCells()
                .stream()
                .filter(box -> box.getIndex() == index)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("request index invalid"));
    }

    private boolean checkDiagonal(Map<Integer, Cell> indexNumberToEntityMap, Cell cell, Board board) {
        int row = cell.getIndex() / board.getRows();

        if (cell.getIndex() % board.getColumns() != 0)
            row++;

        return check(indexNumberToEntityMap, cell, row, board.getRows() + 1);
    }

    private boolean checkVertical(Map<Integer, Cell> indexNumberToEntityMap, Cell cell, Board board) {
        int row = cell.getIndex() / board.getRows();

        if (cell.getIndex() % board.getColumns() != 0)
            row++;

        return check(indexNumberToEntityMap, cell, row, board.getColumns());
    }

    private boolean checkHorizontal(Map<Integer, Cell> indexNumberToEntityMap, Cell cell, Board board) {
        int column = cell.getIndex() % board.getColumns();

        if (column == 0)
            column = board.getColumns();

        return check(indexNumberToEntityMap, cell, column, 1);
    }

    private boolean check(Map<Integer, Cell> indexNumberToEntityMap, Cell cell, int caseType, int indexToCheck1) {
        Optional<Cell> cell1 = Optional.empty();
        Optional<Cell> cell2 = Optional.empty();
        Optional<Cell> cell3 = Optional.empty();
        int indexToCheck2 = indexToCheck1 * 2;

        switch (caseType) {
            case 1:
                cell1 = Optional.of(cell);
                cell2 = Optional.ofNullable(indexNumberToEntityMap.get(cell.getIndex() + indexToCheck1));
                cell3 = Optional.ofNullable(indexNumberToEntityMap.get(cell.getIndex() + indexToCheck2));
                break;
            case 2:
                cell1 = Optional.ofNullable(indexNumberToEntityMap.get(cell.getIndex() - indexToCheck1));
                cell2 = Optional.of(cell);
                cell3 = Optional.ofNullable(indexNumberToEntityMap.get(cell.getIndex() + indexToCheck1));
                break;
            case 3:
                cell1 = Optional.ofNullable(indexNumberToEntityMap.get(cell.getIndex() - indexToCheck2));
                cell2 = Optional.ofNullable(indexNumberToEntityMap.get(cell.getIndex() - indexToCheck1));
                cell3 = Optional.of(cell);
                break;
        }

        return isSamePlayer(cell1, cell2, cell3)
                && isSameMove(cell1, cell2, cell3);
    }

    public boolean isSamePlayer(Optional<Cell> indexBox, Optional<Cell> indexBox2, Optional<Cell> indexBox3) {
        Optional<Long> playerId1 = indexBox
                .map(Cell::getPlayer)
                .map(Player::getId);
        Optional<Long> playerId2 = indexBox2
                .map(Cell::getPlayer)
                .map(Player::getId);
        Optional<Long> playerId3 = indexBox3
                .map(Cell::getPlayer)
                .map(Player::getId);

        return playerId1.isPresent()
                && playerId2.isPresent()
                && playerId3.isPresent()
                && playerId1.get().equals(playerId2.get())
                && playerId2.get().equals(playerId3.get());
    }

    public boolean isSameMove(Optional<Cell> indexBox, Optional<Cell> indexBox2, Optional<Cell> indexBox3) {
        Optional<PlayMove> playMove1 = indexBox
                .map(Cell::getPlayMove);
        Optional<PlayMove> playMove2 = indexBox2
                .map(Cell::getPlayMove);
        Optional<PlayMove> playMove3 = indexBox3
                .map(Cell::getPlayMove);

        return playMove1.isPresent()
                && playMove2.isPresent()
                && playMove3.isPresent()
                && playMove1.get().equals(playMove2.get())
                && playMove2.get().equals(playMove3.get());
    }

}
