package com.example.tic_tac_toe.component;

import com.example.tic_tac_toe.exception.BadException;
import com.example.tic_tac_toe.model.PlayMove;
import com.example.tic_tac_toe.model.entity.Board;
import com.example.tic_tac_toe.model.entity.IndexBox;
import com.example.tic_tac_toe.model.entity.Player;
import com.example.tic_tac_toe.model.request.PlayRequest;
import com.example.tic_tac_toe.repository.BoardRepository;
import com.example.tic_tac_toe.repository.IndexBoxRepository;
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
    private final IndexBoxRepository indexBoxRepository;

    public boolean play(PlayRequest request, Board board, Player player) throws BadException {
        IndexBox indexBox = getIndexBox(request.getIndex(), board);

        if (indexBox.getPlayMove() != null || indexBox.getPlayer() != null)
            throw new BadException("Index box already taken!");

        indexBox.setPlayMove(request.getPlayMove());
        indexBox.setPlayer(player);
        indexBoxRepository.save(indexBox);

        return isWon(indexBox, board);
    }

    public void closeGame(Board board) {
        board.setActive(false);
        boardRepository.save(board);
    }

    private boolean isWon(IndexBox indexBox, Board board) {
        Map<Integer, IndexBox> indexNumberToEntityMap = getIndexNumberToEntityMap(board);
        boolean checkedHorizontal = checkHorizontal(indexNumberToEntityMap, indexBox, board);
        boolean checkedVertical = checkVertical(indexNumberToEntityMap, indexBox, board);
        boolean checkedDiagonal = checkDiagonal(indexNumberToEntityMap, indexBox,board);

        return checkedHorizontal || checkedVertical || checkedDiagonal;
    }

    private Map<Integer, IndexBox> getIndexNumberToEntityMap(Board board) {
        return board.getIndexBoxes()
                .stream()
                .collect(Collectors.toMap(IndexBox::getIndex, Function.identity()));
    }

    private IndexBox getIndexBox(int index, Board board) {
        return board.getIndexBoxes()
                .stream()
                .filter(box -> box.getIndex() == index)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("request index invalid"));
    }

    private boolean checkDiagonal(Map<Integer, IndexBox> indexNumberToEntityMap, IndexBox indexBox, Board board) {
        int row = indexBox.getIndex() / board.getRows();

        if (indexBox.getIndex() % board.getColumns() != 0)
            row++;

        return check(indexNumberToEntityMap, indexBox, row, board.getRows() + 1);
    }

    private boolean checkVertical(Map<Integer, IndexBox> indexNumberToEntityMap, IndexBox indexBox, Board board) {
        int row = indexBox.getIndex() / board.getRows();

        if (indexBox.getIndex() % board.getColumns() != 0)
            row++;

        return check(indexNumberToEntityMap, indexBox, row, board.getColumns());
    }

    private boolean checkHorizontal(Map<Integer, IndexBox> indexNumberToEntityMap, IndexBox indexBox, Board board) {
        int column = indexBox.getIndex() % board.getColumns();

        if (column == 0)
            column = board.getColumns();

        return check(indexNumberToEntityMap, indexBox, column, 1);
    }

    private boolean check(Map<Integer, IndexBox> indexNumberToEntityMap, IndexBox indexBox, int caseType, int indexToCheck1) {
        Optional<IndexBox> indexBox1 = Optional.empty();
        Optional<IndexBox> indexBox2 = Optional.empty();
        Optional<IndexBox> indexBox3 = Optional.empty();
        int indexToCheck2 = indexToCheck1 * 2;

        switch (caseType) {
            case 1:
                indexBox1 = Optional.of(indexBox);
                indexBox2 = Optional.ofNullable(indexNumberToEntityMap.get(indexBox.getIndex() + indexToCheck1));
                indexBox3 = Optional.ofNullable(indexNumberToEntityMap.get(indexBox.getIndex() + indexToCheck2));
                break;
            case 2:
                indexBox1 = Optional.ofNullable(indexNumberToEntityMap.get(indexBox.getIndex() - indexToCheck1));
                indexBox2 = Optional.of(indexBox);
                indexBox3 = Optional.ofNullable(indexNumberToEntityMap.get(indexBox.getIndex() + indexToCheck1));
                break;
            case 3:
                indexBox1 = Optional.ofNullable(indexNumberToEntityMap.get(indexBox.getIndex() - indexToCheck2));
                indexBox2 = Optional.ofNullable(indexNumberToEntityMap.get(indexBox.getIndex() - indexToCheck1));
                indexBox3 = Optional.of(indexBox);
                break;
        }

        return isSamePlayer(indexBox1, indexBox2, indexBox3)
                && isSameMove(indexBox1, indexBox2, indexBox3);
    }

    public boolean isSamePlayer(Optional<IndexBox> indexBox, Optional<IndexBox> indexBox2, Optional<IndexBox> indexBox3) {
        Optional<Long> playerId1 = indexBox
                .map(IndexBox::getPlayer)
                .map(Player::getId);
        Optional<Long> playerId2 = indexBox2
                .map(IndexBox::getPlayer)
                .map(Player::getId);
        Optional<Long> playerId3 = indexBox3
                .map(IndexBox::getPlayer)
                .map(Player::getId);

        return playerId1.isPresent()
                && playerId2.isPresent()
                && playerId3.isPresent()
                && playerId1.get().equals(playerId2.get())
                && playerId2.get().equals(playerId3.get());
    }

    public boolean isSameMove(Optional<IndexBox> indexBox, Optional<IndexBox> indexBox2, Optional<IndexBox> indexBox3) {
        Optional<PlayMove> playMove1 = indexBox
                .map(IndexBox::getPlayMove);
        Optional<PlayMove> playMove2 = indexBox2
                .map(IndexBox::getPlayMove);
        Optional<PlayMove> playMove3 = indexBox3
                .map(IndexBox::getPlayMove);

        return playMove1.isPresent()
                && playMove2.isPresent()
                && playMove3.isPresent()
                && playMove1.get().equals(playMove2.get())
                && playMove2.get().equals(playMove3.get());
    }

}
