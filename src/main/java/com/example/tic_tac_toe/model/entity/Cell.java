package com.example.tic_tac_toe.model.entity;

import com.example.tic_tac_toe.model.PlayMove;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.sql.Timestamp;

@Entity
@Table(name = "cell")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
// todo:: add constraint index && board id - cant have 2 indexBox with same index and board id
public class Cell {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    private Timestamp createAt;

    @Column(nullable = false)
    private Integer index;

    @Column
    private PlayMove playMove;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;
}


