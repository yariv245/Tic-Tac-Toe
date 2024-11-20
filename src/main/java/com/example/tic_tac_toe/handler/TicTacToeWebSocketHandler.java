package com.example.tic_tac_toe.handler;

import com.example.tic_tac_toe.component.BoardComponent;
import com.example.tic_tac_toe.component.PlayMoveComponent;
import com.example.tic_tac_toe.component.PlayerComponent;
import com.example.tic_tac_toe.exception.BadException;
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
    private final Map<String, Long> sessionIdToBoardIdMap = new HashMap<>();
    private final Map<String, Player> sessionIdToPlayerMap = new HashMap<>();
    private final Map<Long, List<WebSocketSession>> boardIdToSessionsMap = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PlayerComponent playerComponent;
    private final BoardComponent boardComponent;
    private final PlayMoveComponent playMoveComponent;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("New Session, id:{}", session.getId());
        String userName = getUserNameFromSession(session);
        Player player = playerComponent.getPlayer(userName);
        Board board = boardComponent.addPlayerToBoard(player);

        sessionIdToPlayerMap.put(session.getId(), player);
        boardIdToBoardMap.put(board.getId(), board);
        sessionIdToBoardIdMap.put(session.getId(), board.getId());
        putToMap(boardIdToSessionsMap, session, board);
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
            boolean won = playMoveComponent.play(playRequest, board, player);
            String response = getResponse(session, playRequest, won, player);
            List<WebSocketSession> webSocketSessions = boardIdToSessionsMap.get(board.getId());
            sendMessages(session, webSocketSessions, response);
            if (won) {
                closeSessions(webSocketSessions);
                playMoveComponent.closeGame(board);
            }
        } catch (BadException e) {
            log.error("Bad Exception Thrown ");
            session.sendMessage(new TextMessage(e.getMessage()));
        }
    }

    private void closeSessions(List<WebSocketSession> webSocketSessions) throws IOException {
        for (WebSocketSession session : webSocketSessions) {
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

    private void validateRequest(PlayRequest playRequest) throws BadException {
        if (playRequest.getIndex() < 1 || playRequest.getIndex() > 9)
            throw new BadException("index must be 1-9");

        if (playRequest.getPlayMove() == null)
            throw new BadException("playMove can't be null !");
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
        boardIdToSessionsMap.get(boardId).remove(session);
        sessionIdToPlayerMap.remove(session.getId());
        sessionIdToBoardIdMap.remove(session.getId());
    }

    private String getUserNameFromSession(WebSocketSession session) throws Exception {

        return Optional.of(session)
                .map(WebSocketSession::getHandshakeHeaders)
                .map(t -> t.get("username"))
                .map(collection -> collection.stream().findFirst())
                .filter(Optional::isPresent)  // Check if the Optional contains a value
                .map(Optional::get)
                .orElseThrow(() -> new Exception(("NO userName found on connect session")));
    }
}