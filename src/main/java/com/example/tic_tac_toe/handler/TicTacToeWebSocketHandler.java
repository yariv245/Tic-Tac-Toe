package com.example.tic_tac_toe.handler;

import com.example.tic_tac_toe.component.BoardComponent;
import com.example.tic_tac_toe.component.CaffeineCacheComponent;
import com.example.tic_tac_toe.component.PlayMoveComponent;
import com.example.tic_tac_toe.component.PlayerComponent;
import com.example.tic_tac_toe.exception.BadRequestException;
import com.example.tic_tac_toe.exception.BusinessException;
import com.example.tic_tac_toe.model.entity.Board;
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

import java.io.IOException;
import java.util.*;

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
            caffeineCacheComponent.put(BOARD_ID_TO_PLAYER_TURN, board.getId().toString(), firstPlayerUserName);
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
        Board board = caffeineCacheComponent.find(BOARD_ID_TO_BOARD, boardId.toString(), Board.class)
                .orElseThrow();
        Player player = caffeineCacheComponent.find(USERNAME_TO_PLAYER, playerUserName, Player.class)
                .orElseThrow();
        String playerUserNameTurn = caffeineCacheComponent.find(BOARD_ID_TO_PLAYER_TURN, board.getId().toString(), String.class)
                .orElseThrow();

        if (!Objects.equals(playerUserNameTurn, player.getUserName())) {
            session.sendMessage(new TextMessage("It's not your TURN !"));
            return;
        }

        boolean won = playMoveComponent.play(playRequest, board, player);
        String response = getResponse(session, playRequest, won, player);
        List<WebSocketSession> webSocketSessions = caffeineCacheComponent.find(BOARD_ID_TO_SESSIONS, board.getId().toString(), List.class)
                .orElseThrow();
        sendMessages(session, webSocketSessions, response);

        if (won) {
            closeSessions(session, webSocketSessions);
            playMoveComponent.closeGame(board);
            session.close(CloseStatus.NORMAL);
        } else {
            String opponentUserName = getOpponentUserName(player, board);
            caffeineCacheComponent.put(BOARD_ID_TO_PLAYER_TURN, board.getId().toString(), opponentUserName);
        }

    }

    private String getOpponentUserName(Player player, Board board) {
        return board.getPlayers()
                .stream()
                .filter(t -> !Objects.equals(t.getUserName(), player.getUserName()))
                .findFirst()
                .map(Player::getUserName)
                .orElseThrow(() -> new BusinessException(OPPONENT_NOT_FOUND_MESSAGE));
    }

    private void closeSessions(WebSocketSession currentSession, List<WebSocketSession> webSocketSessions) throws IOException {
        for (WebSocketSession session : webSocketSessions) {
            if (!session.getId().equals(currentSession.getId()))
                session.close(CloseStatus.NORMAL);
        }
    }

    private void sendMessages(WebSocketSession session, List<WebSocketSession> webSocketSessions, String response) throws IOException {
        for (WebSocketSession webSocketSession : webSocketSessions) {
            if (webSocketSession.isOpen() && !webSocketSession.getId().equals(session.getId())) {
                webSocketSession.sendMessage(new TextMessage(response));
            }
        }
    }

    private void validateRequest(PlayRequest playRequest) {

        if (playRequest.getIndex() < 1 || playRequest.getIndex() > 9)
            throw new BadRequestException(INDEX_ERROR_MESSAGE);

        if (playRequest.getPlayMove() == null)
            throw new BadRequestException(PLAY_MOVE_ERROR_MESSAGE);
    }

    private String getResponse(WebSocketSession session, PlayRequest playRequest, boolean won, Player player) throws IOException {
        String response = objectMapper.writeValueAsString(playRequest);

        if (won) {
            log.info("player won!");
            session.sendMessage(new TextMessage("You are the WINNER !!"));
            response = player.getUserName() + " WON !!";
        }

        return response;
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