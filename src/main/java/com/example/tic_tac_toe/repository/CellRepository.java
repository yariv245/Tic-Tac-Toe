package com.example.tic_tac_toe.repository;

import com.example.tic_tac_toe.model.entity.Cell;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CellRepository extends JpaRepository<Cell, Long> {
}
