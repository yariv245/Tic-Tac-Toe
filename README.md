# Tic Tac Toe - Real-Time Multiplayer Game

This project implements a real-time multiplayer Tic Tac Toe game using **Java**, **Spring Boot**, and **H2 Database**. Players can connect to the game via WebSocket, authenticate themselves, and play against each other in real-time. The game ensures proper validation, turn-based gameplay, and win condition checks.

## Entities

### Player
- Represents a user who connects to the game.
- Attributes:
  - `userName` : Unique identifier for the player.
  - `password` : Used for authentication.
  - `boards` : List of board player associated.
  - `cells` : List of cell player associated.

### Board
- Represents the game board - in our case Tic Tac Toe.
- Attributes:
   - `player` : A list of Players (up to 2).
   - `cells` : A 2D array representing rows and columns of the board.
   - `rows` : number of rows.
   - `columns` : number of columns.

### Cell
- Represents each cell on the board.
- Attributes:
   - `index` : Position of the box (0–8).
   - `playMove`: Current state of the cell (empty or occupied by X or O).
   - `player` : A player did the playmove.
   - `board` : board the cell is related to.

## Game Flow
### 1. Player Authentication
- A player connects to the WebSocket server with userName and password in the headers.
- The server:
   1. Searches the database for a player with the given userName.
   2. If the player does not exist, a new player is created with the provided password.
   3. If the player exists, the password is validated:
      - If valid, the player is connected to the game.
      - If invalid, an error is sent to the player.

### 2. Assigning Players to Boards
- The server:
   1. Finds an active board with fewer than two players:
      - If an active board is found, the player is added to it.
      - If no active board exists, a new board is created, and the player is assigned to it.
   2. Manages turns by assigning the first player to start.

### 3. Player Move
- Players send messages with their desired move, e.g., { "index": 8, "playMove": "O" }.
- The server:
   1. Validates the move:
       - Ensures the move is being made during the player's turn.
       - Checks that the selected IndexBox is not already occupied.
       - If invalid, sends an error message to the player.
   2. Updates the board with the move.
   3. Checks for a win condition:
      - Horizontal, Vertical, and Diagonal checks are performed.
      - If a win condition is met:
        - Sends a "Win" message to the winner.
        - Sends a message to the other player indicating the winner.
      - If no win and the board is full, sends a "Draw" message to both players.
    4. Switches to the next player's turn.

### 4. Caching and Validation
- Cached active boards for efficient access.
- Validates requests to ensure moves are within bounds and adhere to game rules.
- Ensures messages are only sent to players connected to the same board. 

## WebSocket API
### Connection
- **URL**: `ws://localhost:8080/tictactoe`
- **Headers**:
  - `userName` : The player's name.
  - `password` : The player's password (required for authentication).
### Messages
Players communicate via JSON messages with the following structure:
- `index` : The position (1–9) on the board.
- `playMove` : The move, either "X" or "O".
```bash
{
  "index": 8,
  "playMove": "O"
}
```

## How to Run
### Prerequisites
- Java 17 or above.
- Maven installed.
- WebSocket client (e.g., Postman, browser extension, or custom frontend).

### Steps
1. **Clone the repository:**
```bash
git clone https://github.com/yariv245/tic-tac-toe.git
cd tic-tac-toe
```
2. **Build the project:**
```bash
mvn clean install
```
3. **Run the server:**
```bash
mvn spring-boot:run
```
4. **Connect players:**
- Use a WebSocket client to connect to `ws://localhost:8080/tictactoe`.
- Add headers:
  - `userName`: Player's user name.
  - `password`: Player's password.
5. **Play the game:**
  - Send messages in the format described above to make moves.
  - Monitor game progress and results in the WebSocket client.

### Features
- Real-time gameplay for two players.
- Password-based authentication for players.
- Turn-based logic ensures players can only play during their turn.
- Board structure supports rows and columns for better management.
- Comprehensive win condition checks (horizontal, vertical, diagonal).
- Validation of moves to prevent invalid inputs or duplicate moves.
- Efficient caching for active boards.

### Improvements and Next Steps
- Cache
- Enhance error handling and messaging for a smoother player experience.
- Implement a timeout mechanism to handle inactive players.
