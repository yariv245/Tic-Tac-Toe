package com.example.tic_tac_toe.model;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PlayerTurn implements Serializable {
    private String userName;
    private PlayMove playMove;
}
