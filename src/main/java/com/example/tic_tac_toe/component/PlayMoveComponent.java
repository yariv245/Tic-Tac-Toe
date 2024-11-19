package com.example.tic_tac_toe.component;

import com.example.tic_tac_toe.exception.BadException;
import com.example.tic_tac_toe.model.PlayMove;
import com.example.tic_tac_toe.model.entity.Board;
import com.example.tic_tac_toe.model.entity.IndexBox;
import com.example.tic_tac_toe.model.entity.Player;
import com.example.tic_tac_toe.model.request.PlayRequest;
import com.example.tic_tac_toe.repository.BoardRepository;
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

    public boolean play(PlayRequest request, Board board, Player player) throws BadException {
        IndexBox indexBox = getIndexBox(request.getIndex(), board);

        if (indexBox.getPlayMove() != null || indexBox.getPlayer() != null)
            throw new BadException("Index box already taken!");

        indexBox.setPlayMove(request.getPlayMove());
        indexBox.setPlayer(player);

        boolean won = isWon(indexBox, board);
        if (won) {
            board.setActive(false);
        }
        boardRepository.save(board);
        return won;
    }

    private boolean isWon(IndexBox indexBox, Board board) {
        Map<Integer, IndexBox> indexNumberToEntityMap = getIndexNumberToEntityMap(board);
        boolean checkedHorizontal = checkHorizontal(indexNumberToEntityMap, indexBox);
        boolean checkedVertical = checkVertical(indexNumberToEntityMap, indexBox);
        boolean checkedDiagonal = checkDiagonal(indexNumberToEntityMap, indexBox);

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

    private boolean checkDiagonal(Map<Integer, IndexBox> indexNumberToEntityMap, IndexBox indexBox) {
        int row = indexBox.getIndex() / 3;
        if (indexBox.getIndex() % 3 != 0)
            row++;

        return check(indexNumberToEntityMap, indexBox, row,4,4);
    }

    private boolean checkVertical(Map<Integer, IndexBox> indexNumberToEntityMap, IndexBox indexBox) {
        int row = indexBox.getIndex() / 3;

        if (indexBox.getIndex() % 3 != 0)
            row++;

        return check(indexNumberToEntityMap, indexBox, row,3,6);
    }

    private boolean checkHorizontal(Map<Integer, IndexBox> indexNumberToEntityMap, IndexBox indexBox) {
        int column = indexBox.getIndex() % 3;

        if (column == 0)
            column = 3;

        return check(indexNumberToEntityMap, indexBox, column,1,2);
    }

    private boolean check(Map<Integer, IndexBox> indexNumberToEntityMap, IndexBox indexBox, int caseType, int indexToCheck1, int indexToCheck2) {
        Optional<PlayMove> move1 = null;
        Optional<PlayMove> move2 = null;
        Optional<PlayMove> move3 = null;

        switch (caseType) {
            case 1:
                move1 = Optional.of(indexBox.getPlayMove());
                move2 = Optional.of(indexNumberToEntityMap.get(indexBox.getIndex() + indexToCheck1)).map(IndexBox::getPlayMove);
                move3 = Optional.of(indexNumberToEntityMap.get(indexBox.getIndex() + indexToCheck2)).map(IndexBox::getPlayMove);
                break;
            case 2:
                move1 = Optional.of(indexNumberToEntityMap.get(indexBox.getIndex() - indexToCheck1)).map(IndexBox::getPlayMove);
                move2 = Optional.of(indexBox.getPlayMove());
                move3 = Optional.of(indexNumberToEntityMap.get(indexBox.getIndex() + indexToCheck1)).map(IndexBox::getPlayMove);
                break;
            case 3:
                move1 = Optional.of(indexNumberToEntityMap.get(indexBox.getIndex() - indexToCheck2)).map(IndexBox::getPlayMove);
                move2 = Optional.of(indexNumberToEntityMap.get(indexBox.getIndex() - indexToCheck1)).map(IndexBox::getPlayMove);
                move3 = Optional.of(indexBox.getPlayMove());
                break;
        }

        return move1.isPresent()
                && move2.isPresent()
                && move3.isPresent()
                && move1.get() == move3.get()
                && move2.get() == move3.get();
    }
}
