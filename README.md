This is a tic tac toe game:

we have 3 entities:
Player - the user who connect
Board - board game htat have list of players and list of idexBox
IndexBox - present each box on the board - in our case there will be 9.

On each connect request with the param userName, db will search the player by userName if doesn't exist it will create it.
Next, we query the DB to find active board with less than 2 player.
If not found one, default tic tac toe board will be created and assign the player to it.

On each play move-> find the board and try to assign the move, if the indexBox already taken(another player already used this box) error will be sent to the player.
After assign the move-> 3 checks will be done - horizntal vertical and diagonal.
On each case the row/column will be calcualted and check the case if the player won.

if player won he will get message he won and the other player will get what player won.

Improvments:
Use cache instead of maps.
Validate request.
Constants.
Mediator pattern.
Send message to only player on the same board.

Project is in Java,spring boot,H2.

To connect:
URL ws://localhost:8080/tictactoe
add to header -> userName

Each message:
{"index": 8, "playMove": "O"}
