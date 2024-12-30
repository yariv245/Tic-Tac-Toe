package com.example.tic_tac_toe.component;

import com.example.tic_tac_toe.model.entity.Player;

public interface PlayerComponent {
    Player getPlayer(String userName, String password);

    Player save(Player player);
}
