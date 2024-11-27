package com.example.tic_tac_toe.handler;

import com.example.tic_tac_toe.component.BoardComponent;
import com.example.tic_tac_toe.component.PlayMoveComponent;
import com.example.tic_tac_toe.component.PlayerComponent;
import com.example.tic_tac_toe.exception.BadException;
import com.example.tic_tac_toe.exception.BadRequestException;
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

@Component
@Slf4j
@RequiredArgsConstructor
public class TicTacToeWebSocketHandler extends TextWebSocketHandler {

    private final Map<Long, Board> boardIdToBoardMap = new HashMap<>();
    private final Map<Long, Long> boardIdToPlayerTurnMap = new HashMap<>();
    private final Map<String, Long> sessionIdToBoardIdMap = new HashMap<>();
    private final Map<String, Player> sessionIdToPlayerMap = new HashMap<>();
    private final Map<Long, List<WebSocketSession>> boardIdToSessionsMap = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PlayerComponent playerComponent;
    private final BoardComponent boardComponent;
    private final PlayMoveComponent playMoveComponent;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            log.info("New Session, id:{}", session.getId());
            String userName = getFromSession(session, "userName");
            String password = getFromSession(session, "password");
            Player player = playerComponent.getPlayer(userName, password);
            Board board = boardComponent.addPlayerToBoard(player);

            sessionIdToPlayerMap.put(session.getId(), player);
            boardIdToBoardMap.put(board.getId(), board);
            sessionIdToBoardIdMap.put(session.getId(), board.getId());
            putToMap(boardIdToSessionsMap, session, board);
            if (board.getPlayers().size() == 2) {
                Long firstPlayerId = getFirstPlayerId(board);
                boardIdToPlayerTurnMap.put(board.getId(), firstPlayerId);
            }
        } catch (BadException e) {
            log.error("Bad Exception Thrown " + e.getMessage());
            session.sendMessage(new TextMessage(e.getMessage()));
            session.close();
        }
    }

    private static Long getFirstPlayerId(Board board) throws BadException {
        return board.getPlayers()
                .stream()
                .findFirst()
                .map(Player::getId)
                .orElseThrow(() -> new BadException("Couldn't find first player id"));
    }

    private void putToMap(Map<Long, List<WebSocketSession>> boardIdToSessionsMap, WebSocketSession session, Board board) {
        if (!boardIdToSessionsMap.containsKey(board.getId())) {
            ArrayList<WebSocketSession> webSocketSessions = new ArrayList<>();
            boardIdToSessionsMap.put(board.getId(), webSocketSessions);
        }
        boardIdToSessionsMap.get(board.getId()).add(session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            PlayRequest playRequest = objectMapper.readValue(message.getPayload(), PlayRequest.class);
            validateRequest(playRequest);
            Board board = boardIdToBoardMap.get(sessionIdToBoardIdMap.get(session.getId()));
            Player player = sessionIdToPlayerMap.get(session.getId());
            Long playerIdTurn = boardIdToPlayerTurnMap.get(board.getId());

            if (!Objects.equals(playerIdTurn, player.getId())) {
                session.sendMessage(new TextMessage("It's not your TURN !"));
                return;
            }

            boolean won = playMoveComponent.play(playRequest, board, player);
            String response = getResponse(session, playRequest, won, player);
            List<WebSocketSession> webSocketSessions = boardIdToSessionsMap.get(board.getId());
            sendMessages(session, webSocketSessions, response);
            if (won) {
                closeSessions(session, webSocketSessions);
                playMoveComponent.closeGame(board);
                session.close();
            } else {
                Long opponentId = getOpponentId(player, board);
                boardIdToPlayerTurnMap.put(board.getId(), opponentId);
            }
        } catch (BadRequestException e) {
            log.error("Bad Request Thrown " + e.getMessage());
            session.sendMessage(new TextMessage(e.getMessage()));
            session.close();
        } catch (BadException e) {
            log.error("Bad Exception Thrown " + e.getMessage());
            session.sendMessage(new TextMessage(e.getMessage()));
        }
    }

    private Long getOpponentId(Player player, Board board) throws BadException {
        return board.getPlayers()
                .stream()
                .filter(t -> !Objects.equals(t.getId(), player.getId()))
                .findFirst()
                .map(Player::getId)
                .orElseThrow(() -> new BadException("coludn't find any Opponent to player:" + player.getUserName()));
    }

    private void closeSessions(WebSocketSession currentSession, List<WebSocketSession> webSocketSessions) throws IOException {
        for (WebSocketSession session : webSocketSessions) {
            if (!session.getId().equals(currentSession.getId()))
                session.close();
        }
    }

    private void sendMessages(WebSocketSession session, List<WebSocketSession> webSocketSessions, String response) throws IOException {
        for (WebSocketSession webSocketSession : webSocketSessions) {
            if (webSocketSession.isOpen() && !webSocketSession.getId().equals(session.getId())) {
                webSocketSession.sendMessage(new TextMessage(response));
            }
        }
    }

    private void validateRequest(PlayRequest playRequest) throws BadRequestException {
        if (playRequest.getIndex() < 1 || playRequest.getIndex() > 9)
            throw new BadRequestException("index must be 1-9");

        if (playRequest.getPlayMove() == null)
            throw new BadRequestException("playMove can't be null !");
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
        Long boardId = sessionIdToBoardIdMap.get(session.getId());
        Optional.ofNullable(boardIdToSessionsMap.get(boardId))
                .map(sessions -> sessions.remove(session));
        sessionIdToPlayerMap.remove(session.getId());
        sessionIdToBoardIdMap.remove(session.getId());
    }

    private String getFromSession(WebSocketSession session, String header) throws Exception {

        return Optional.of(session)
                .map(WebSocketSession::getHandshakeHeaders)
                .map(t -> t.get(header))
                .map(collection -> collection.stream().findFirst())
                .filter(Optional::isPresent)  // Check if the Optional contains a value
                .map(Optional::get)
                .orElseThrow(() -> new Exception(("NO passowrd found on connect session")));
    }
}