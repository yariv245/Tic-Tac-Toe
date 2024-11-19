package com.example.tic_tac_toe;

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

    private final List<WebSocketSession> sessions = new ArrayList<>();
    private final Map<Long, Board> boardIdToBoardMap = new HashMap<>();
    private final Map<String, Long> sessionIdToBoardIdMap = new HashMap<>();
    private final Map<String, Player> sessionIdToPlayerMap = new HashMap<>();
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

        sessions.add(session);
        sessionIdToPlayerMap.put(session.getId(), player);
        boardIdToBoardMap.put(board.getId(), board);
        sessionIdToBoardIdMap.put(session.getId(), board.getId());
    }

    @Override
    // todo:: validate request - bonus
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            PlayRequest playRequest = objectMapper.readValue(message.getPayload(), PlayRequest.class);
            Board board = boardIdToBoardMap.get(sessionIdToBoardIdMap.get(session.getId()));
            Player player = sessionIdToPlayerMap.get(session.getId());
            boolean won = playMoveComponent.play(playRequest, board, player);
            String response = getResponse(session, playRequest, won, player);
            for (WebSocketSession webSocketSession : sessions) {
                if (webSocketSession.isOpen() && !webSocketSession.getId().equals(session.getId())) {
                    webSocketSession.sendMessage(new TextMessage(response));
                }
            }
        } catch (BadException e) {
            log.error("Bad Exception Thrown ");
            session.sendMessage(new TextMessage(e.getMessage()));
        }
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
        sessions.remove(session);
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