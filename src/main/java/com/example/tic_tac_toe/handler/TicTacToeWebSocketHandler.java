package com.example.tic_tac_toe.handler;

import com.example.tic_tac_toe.component.*;
import com.example.tic_tac_toe.exception.BadRequestException;
import com.example.tic_tac_toe.exception.BusinessException;
import com.example.tic_tac_toe.model.PlayMove;
import com.example.tic_tac_toe.model.PlayerTurn;
import com.example.tic_tac_toe.model.entity.Board;
import com.example.tic_tac_toe.model.entity.Cell;
import com.example.tic_tac_toe.model.entity.Player;
import com.example.tic_tac_toe.model.request.PlayRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.example.tic_tac_toe.util.CacheConstant.*;
import static com.example.tic_tac_toe.util.ErrorMessageConstants.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class TicTacToeWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PlayerComponent playerComponent;
    private final BoardComponent boardComponent;
    private final PlayMoveComponent playMoveComponent;
    private final CaffeineCacheComponent caffeineCacheComponent;
    private final SessionComponent sessionComponent;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("New Session, id:{}", session.getId());
        String userName = getFromSession(session, "userName");
        String password = getFromSession(session, "password");
        Player player = playerComponent.getPlayer(userName, password);
        Board board = boardComponent.addPlayerToBoard(player);
        putToMap(session, board);

        if (board.getPlayers().size() == 2) {
            String firstPlayerUserName = getFirstPlayerUserName(board);
            PlayerTurn playerTurn = PlayerTurn.builder()
                    .userName(firstPlayerUserName)
                    .playMove(PlayMove.X)
                    .build();
            caffeineCacheComponent.put(BOARD_ID_TO_PLAYER_TURN, board.getId().toString(), playerTurn);
        }
    }

    private String getFirstPlayerUserName(Board board) {
        return board.getPlayers()
                .stream()
                .findFirst()
                .map(Player::getUserName)
                .orElseThrow(() -> new BusinessException(FIRST_PLAYER_NOT_FOUND_MESSAGE));
    }

    private void putToMap(WebSocketSession session, Board board) {
        Optional<List> webSocketSessions = caffeineCacheComponent.find(BOARD_ID_TO_SESSIONS, board.getId().toString(), List.class);

        if (webSocketSessions.isEmpty()) {
            webSocketSessions = Optional.of(new ArrayList<WebSocketSession>());
            caffeineCacheComponent.put(BOARD_ID_TO_SESSIONS, board.getId().toString(), webSocketSessions.get());

        }
        webSocketSessions.get().add(session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        PlayRequest playRequest = objectMapper.readValue(message.getPayload(), PlayRequest.class);
        validateRequest(playRequest);
        String playerUserName = getFromSession(session, "userName");
        Long boardId = caffeineCacheComponent.find(USERNAME_TO_BOARD_ID, playerUserName, Long.class)
                .orElseThrow();
        PlayerTurn playerUserNameTurn = caffeineCacheComponent.find(BOARD_ID_TO_PLAYER_TURN, boardId.toString(), PlayerTurn.class)
                .orElseThrow();

        if (!Objects.equals(playerUserNameTurn.getUserName(), playerUserName)) {
            session.sendMessage(new TextMessage("It's not your TURN !"));
            return;
        }

        if (!Objects.equals(playerUserNameTurn.getPlayMove(), playRequest.getPlayMove())) {
            session.sendMessage(new TextMessage("Wrong play Move entered !"));
            return;
        }

        Board board = caffeineCacheComponent.find(BOARD_ID_TO_BOARD, boardId.toString(), Board.class)
                .orElseThrow();
        Player player = caffeineCacheComponent.find(USERNAME_TO_PLAYER, playerUserName, Player.class)
                .orElseThrow();
        Cell cell = playMoveComponent.play(playRequest, board, player);
        List<WebSocketSession> webSocketSessions = caffeineCacheComponent.find(BOARD_ID_TO_SESSIONS, board.getId().toString(), List.class)
                .orElseThrow();

        if (playMoveComponent.isWon(cell, board)) {
            log.info("player won!");
            session.sendMessage(new TextMessage("You are the WINNER !!"));
            sessionComponent.sendMessages(session, webSocketSessions, player.getUserName() + " WON !!");
            sessionComponent.closeSessions(session, webSocketSessions);
            boardComponent.closeGame(board);
            session.close(CloseStatus.NORMAL);
        }
        if (playMoveComponent.isDraw(board, playRequest.getPlayMove())) {
            log.info("!! DRAW !!");
            sessionComponent.sendMessages(webSocketSessions, "DRAW !");
            sessionComponent.closeSessions(session, webSocketSessions);
            boardComponent.closeGame(board);
            session.close(CloseStatus.NORMAL);
        } else {
            String response = objectMapper.writeValueAsString(playRequest);
            sessionComponent.sendMessages(session, webSocketSessions, response);
            updateNextPlayerTurn(playerUserNameTurn, board);
        }

    }

    private void updateNextPlayerTurn(PlayerTurn currentPlayer, Board board) {
        String opponentUserName = getOpponentUserName(currentPlayer.getUserName(), board);
        PlayMove opponentPlayMove = PlayMove.findOpponentPlayMove(currentPlayer.getPlayMove())
                .orElseThrow(() -> new BusinessException("Couldn't find Opponent play move"));
        PlayerTurn playerTurn = PlayerTurn.builder()
                .userName(opponentUserName)
                .playMove(opponentPlayMove)
                .build();
        caffeineCacheComponent.put(BOARD_ID_TO_PLAYER_TURN, board.getId().toString(), playerTurn);
    }

    private String getOpponentUserName(String playerUserName, Board board) {
        return board.getPlayers()
                .stream()
                .filter(t -> !Objects.equals(t.getUserName(), playerUserName))
                .findFirst()
                .map(Player::getUserName)
                .orElseThrow(() -> new BusinessException(OPPONENT_NOT_FOUND_MESSAGE));
    }

    private void validateRequest(PlayRequest playRequest) {

        if (playRequest.getIndex() < 1 || playRequest.getIndex() > 9)
            throw new BadRequestException(INDEX_ERROR_MESSAGE);

        if (playRequest.getPlayMove() == null)
            throw new BadRequestException(PLAY_MOVE_ERROR_MESSAGE);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("Session CLOSED, id:{}", session.getId());
        String username = getFromSession(session, "username");
        caffeineCacheComponent.find(USERNAME_TO_BOARD_ID, username, Long.class)
                .ifPresent(boardId -> {
                    caffeineCacheComponent.evictIfPresent(BOARD_ID_TO_PLAYER_TURN, boardId.toString());
                    caffeineCacheComponent.evictIfPresent(BOARD_ID_TO_SESSIONS, boardId.toString());
                });
        caffeineCacheComponent.evictIfPresent(USERNAME_TO_BOARD_ID, username);
        caffeineCacheComponent.evictIfPresent(USERNAME_TO_PLAYER, username);
    }

    private String getFromSession(WebSocketSession session, String header) {
        return Optional.of(session)
                .map(WebSocketSession::getHandshakeHeaders)
                .map(t -> t.get(header))
                .map(collection -> collection.stream().findFirst())
                .filter(Optional::isPresent)  // Check if the Optional contains a value
                .map(Optional::get)
                .orElseThrow(() -> new BadRequestException(String.format(HEADER_PARAM_NOT_FOUND, header)));
    }
}