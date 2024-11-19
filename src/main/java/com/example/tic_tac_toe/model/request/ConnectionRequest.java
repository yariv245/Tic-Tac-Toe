package com.example.tic_tac_toe.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConnectionRequest {
    private String userName;
    // todo:: If you would like to enter specific board
//    private String boardId;
}
