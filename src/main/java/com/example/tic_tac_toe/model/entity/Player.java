package com.example.tic_tac_toe.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.sql.Timestamp;
import java.util.Set;

@Entity
@Table(name = "player")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    private Timestamp createAt;

    @Column
    private String userName;

    @Column
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Board> boards;

    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
    private Set<Cell> cells;
}
