package com.example.tic_tac_toe.component;

import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;

public interface SessionComponent {

    void sendMessages(WebSocketSession session, List<WebSocketSession> webSocketSessions, String response) throws IOException;

    void sendMessages(List<WebSocketSession> webSocketSessions, String response) throws IOException;

    void closeSessions(WebSocketSession currentSession, List<WebSocketSession> webSocketSessions) throws IOException;
}
