package com.example.tic_tac_toe.repository;

import com.example.tic_tac_toe.model.entity.IndexBox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IndexBoxRepository extends JpaRepository<IndexBox, Long> {
}
