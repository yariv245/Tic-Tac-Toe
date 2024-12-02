package com.example.tic_tac_toe.configuration;

import com.example.tic_tac_toe.controller.CustomErrorHandler;
import com.example.tic_tac_toe.handler.TicTacToeWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final TicTacToeWebSocketHandler webSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new CustomErrorHandler(webSocketHandler), "/tictactoe").setAllowedOrigins("*");
    }
}