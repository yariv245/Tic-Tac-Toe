package com.example.tic_tac_toe.component;

import com.example.tic_tac_toe.exception.BadException;
import com.example.tic_tac_toe.model.entity.Player;
import com.example.tic_tac_toe.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
@RequiredArgsConstructor
public class PlayerComponent {
    private final PlayerRepository playerRepository;

    public Player getPlayer(String userName, String password) throws BadException {
        Optional<Player> byUserName = playerRepository.findByUserName(userName);

        if (byUserName.isEmpty())
            return createPlayer(userName, password);

        return byUserName
                .filter(player -> password.equals(player.getPassword()))
                .orElseThrow(() -> new BadException("Wrong passowrd !"));
    }

    private Player createPlayer(String userName, String password) {
        Player player = Player.builder()
                .userName(userName)
                .password(password)
                .build();

        return playerRepository.save(player);
    }

}
