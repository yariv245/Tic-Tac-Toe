package com.example.tic_tac_toe.model.request;

import com.example.tic_tac_toe.model.PlayMove;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayRequest {
    // 1-9
    private int index;
    private PlayMove playMove;
}
