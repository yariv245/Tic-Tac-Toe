package com.example.tic_tac_toe.component.impl;

import com.example.tic_tac_toe.component.CaffeineCacheComponent;
import com.example.tic_tac_toe.component.PlayerComponent;
import com.example.tic_tac_toe.exception.BadRequestException;
import com.example.tic_tac_toe.model.entity.Player;
import com.example.tic_tac_toe.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.example.tic_tac_toe.util.CacheConstant.USERNAME_TO_PLAYER;
import static com.example.tic_tac_toe.util.ErrorMessageConstants.WRONG_PASSWORD_MESSAGE;


@Component
@RequiredArgsConstructor
public class PlayerComponentImpl implements PlayerComponent {
    private final PlayerRepository playerRepository;
    private final CaffeineCacheComponent caffeineCacheComponent;

    public Player getPlayer(String userName, String password) {
        Optional<Player> cached1Player = caffeineCacheComponent.find(USERNAME_TO_PLAYER, userName, Player.class);

        if (cached1Player.isPresent())
            return cached1Player.get();

        Optional<Player> byUserName = playerRepository.findByUserName(userName);

        if (byUserName.isEmpty())
            return createPlayer(userName, password);

        return byUserName
                .filter(player -> password.equals(player.getPassword()))
                .orElseThrow(() -> new BadRequestException(WRONG_PASSWORD_MESSAGE));
    }

    private Player createPlayer(String userName, String password) {
        return save(Player.builder()
                .userName(userName)
                .password(password)
                .build());
    }

    public Player save(Player player) {
        Player saved = playerRepository.save(player);
        caffeineCacheComponent.put(USERNAME_TO_PLAYER, saved.getUserName(), saved);

        return saved;
    }

}
