package com.example.tic_tac_toe.component;

import com.example.tic_tac_toe.exception.BadException;
import com.example.tic_tac_toe.model.entity.Player;
import com.example.tic_tac_toe.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.example.tic_tac_toe.util.CacheConstant.USERNAME_TO_PLAYER;


@Component
@RequiredArgsConstructor
public class PlayerComponent {
    private final PlayerRepository playerRepository;
    private final CacheManager cacheManager;
    private final CaffeineCacheComponent caffeineCacheComponent;

    public Player getPlayer(String userName, String password) throws BadException {
        Optional<Player> cached1Player = caffeineCacheComponent.find(USERNAME_TO_PLAYER, userName, Player.class);

        if (cached1Player.isPresent())
            return cached1Player.get();

        Optional<Player> byUserName = playerRepository.findByUserName(userName);

        if (byUserName.isEmpty())
            return createPlayer(userName, password);

        return byUserName
                .filter(player -> password.equals(player.getPassword()))
                .orElseThrow(() -> new BadException("Wrong passowrd !"));
    }

    private Player createPlayer(String userName, String password) {
        Player player = playerRepository.save(Player.builder()
                .userName(userName)
                .password(password)
                .build());
        caffeineCacheComponent.put(USERNAME_TO_PLAYER, userName, player);

        return player;
    }

    public void save(Player player) {
        Player saved = playerRepository.save(player);
        caffeineCacheComponent.put(USERNAME_TO_PLAYER, saved.getUserName(), saved);
    }

}
