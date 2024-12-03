package com.example.tic_tac_toe.handler;

import com.example.tic_tac_toe.exception.BadRequestException;
import com.example.tic_tac_toe.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

@Slf4j
public class CustomErrorHandler extends WebSocketHandlerDecorator {

    public CustomErrorHandler(WebSocketHandler delegate) {
        super(delegate);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            getDelegate().afterConnectionEstablished(session);
        } catch (Exception ex) {
            handleException(session, ex);
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        try {
            getDelegate().handleMessage(session, message);
        } catch (Exception ex) {
            handleException(session, ex);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        try {
            getDelegate().handleTransportError(session, exception);
        } catch (Exception ex) {
            handleException(session, ex);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        try {
            getDelegate().afterConnectionClosed(session, closeStatus);
        } catch (Exception ex) {
            if (log.isWarnEnabled()) {
                log.warn("Unhandled exception after connection closed for " + this, ex);
            }
        }
    }


    private void handleException(WebSocketSession session, Throwable exception) {
        log.error(exception.getMessage());
        if (exception instanceof BadRequestException) {
            sendMessage(session, new TextMessage(exception.getMessage()));
            tryCloseSession(session, CloseStatus.BAD_DATA);
        } else if (exception instanceof BusinessException) {
            sendMessage(session, new TextMessage(exception.getMessage()));
        }

    }

    private void tryCloseSession(WebSocketSession session, CloseStatus status) {
        if (session.isOpen()) {
            try {
                session.close(status);
            } catch (Throwable ex) {
                // ignore
            }
        }
    }

    private void sendMessage(WebSocketSession session, WebSocketMessage<?> message) {
        if (session.isOpen()) {
            try {
                session.sendMessage(message);
            } catch (Throwable ex) {
                // ignore
            }
        }
    }

}
