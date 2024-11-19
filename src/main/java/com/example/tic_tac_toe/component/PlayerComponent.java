package com.example.tic_tac_toe.component;

import com.example.tic_tac_toe.model.entity.Player;
import com.example.tic_tac_toe.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class PlayerComponent {
    private final PlayerRepository playerRepository;

    public Player getPlayer(String userName) {
        return playerRepository.findByUserName(userName)
                .orElseGet(() -> createPlayer(userName));
    }

    private Player createPlayer(String userName) {
        Player player = Player.builder().userName(userName).build();

        return playerRepository.save(player);
    }

}
