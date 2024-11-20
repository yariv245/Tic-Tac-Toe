package com.example.tic_tac_toe.repository;

import com.example.tic_tac_toe.model.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query("SELECT b FROM Board b WHERE b.active = true AND size(b.players) < 2")
    Optional<Board> findActiveBoardWithLessThanTwoUsers();

    @Query("SELECT b FROM Board b JOIN b.players p WHERE b.active = true AND p.id = :playerId")
    Optional<Board> findActiveBoardByPlayerId(Long playerId);
}
