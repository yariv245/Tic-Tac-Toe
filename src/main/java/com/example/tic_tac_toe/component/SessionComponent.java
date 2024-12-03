package com.example.tic_tac_toe.component;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;


@Component
@RequiredArgsConstructor
public class SessionComponent {

    public void sendMessages(WebSocketSession session, List<WebSocketSession> webSocketSessions, String response) throws IOException {
        for (WebSocketSession webSocketSession : webSocketSessions) {
            if (webSocketSession.isOpen() && !webSocketSession.getId().equals(session.getId())) {
                webSocketSession.sendMessage(new TextMessage(response));
            }
        }
    }

    public void sendMessages(List<WebSocketSession> webSocketSessions, String response) throws IOException {
        for (WebSocketSession webSocketSession : webSocketSessions) {
            webSocketSession.sendMessage(new TextMessage(response));
        }
    }

    public void closeSessions(WebSocketSession currentSession, List<WebSocketSession> webSocketSessions) throws IOException {
        for (WebSocketSession session : webSocketSessions) {
            if (!session.getId().equals(currentSession.getId()))
                session.close(CloseStatus.NORMAL);
        }
    }
}
