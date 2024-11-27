package com.example.tic_tac_toe.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "board")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @Builder.Default
    private Boolean active = Boolean.TRUE;

    @Column(nullable = false)
    @Builder.Default
    private int rows = 0;

    @Column(nullable = false)
    @Builder.Default
    private int columns = 0;

    @CreatedDate
    private Timestamp createAt;

    @OneToMany(mappedBy = "board", fetch = FetchType.EAGER)
    @Builder.Default
    private Set<Cell> cells = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Player> players;
}
