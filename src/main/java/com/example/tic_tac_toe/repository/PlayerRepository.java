package com.example.tic_tac_toe.repository;

import com.example.tic_tac_toe.model.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByUserName(String userName);
}
