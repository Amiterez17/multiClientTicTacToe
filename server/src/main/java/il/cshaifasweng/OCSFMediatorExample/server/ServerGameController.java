package il.cshaifasweng.OCSFMediatorExample.server;
import il.cshaifasweng.OCSFMediatorExample.entities.GameMessage;
import il.cshaifasweng.OCSFMediatorExample.entities.GameMessage.CommandType;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import java.io.IOException;

public class ServerGameController {

    // when the first player connects, we assign them to playerX, and the second to playerO.
    private ConnectionToClient playerX = null;
    private ConnectionToClient playerO = null;
    private char[][] board = new char[3][3];
    private char currentTurn = 'X';
    private char lastWinner = 'X';

    public ServerGameController() {
        // the controller subscribes to events from the clients, so it can react to their commands.
        EventBus.getDefault().register(this);
        clearBoard();
    }

    // the controller listens to ClientMessageEvents,
    // which are posted by the clients when they send a command to the server.
    @Subscribe
    public synchronized void onClientMessage(ClientMessageEvent event) {
        GameMessage msg = event.getMessage();
        ConnectionToClient client = event.getClient();

        System.out.println("ServerGameController received command: " + msg.getCommandType());

        switch (msg.getCommandType()) {
            case CONNECT:
                handleConnect(client);
                break;

            case MAKE_MOVE:
                handleMakeMove(msg, client);
                break;

            case LEAVE:
                handlePlayerLeave(client);
                break;

            case DISCONNECT:
                handleClientWindowClosed(client);
                break;

             case RESTART:
                handleRestart();
                break;

            default:
                System.out.println("Unhandled command type: " + msg.getCommandType());
                break;
        }
    }

    private synchronized void sendToAll(GameMessage msg)
    {
        try {
            if (playerX != null) {
                playerX.sendToClient(msg);
            }
            if (playerO != null) {
                playerO.sendToClient(msg);
            }
        } catch (IOException e) {
            System.err.println("Error sending message to clients: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private synchronized void sendToIndividual(ConnectionToClient client, GameMessage msg) {
        try {
            if (client != null) {
                client.sendToClient(msg);
            }
        } catch (IOException e) {
            System.err.println("Error sending message to client: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void handleConnect(ConnectionToClient client)
    {
        try {
            // first player connects -> assign them to playerX
            if (playerX == null) {
                playerX = client;
                System.out.println("Player X connected and registered.");

                // send a confirmation: "you connected successfully, you are player X"
                client.sendToClient(new GameMessage(CommandType.CONNECT, "You are player X.", 'X'));
            }
            // second player connects (when X is already taken) -> assign them to playerO
            else if (playerO == null) {
                playerO = client;
                System.out.println("Player O connected. Starting the game!");

                // send a confirmation: "you connected successfully, you are player O"
                client.sendToClient(new GameMessage(CommandType.CONNECT, "You are player O.", 'O'));
            } else {
                // if both playerX and playerO are already taken, we reject the connection
                System.out.println("A third player attempted to connect. Connection rejected.");
                sendToIndividual(client, new GameMessage(CommandType.ERROR, "Game is full!", ' '));
            }
            if (playerX != null && playerO != null) {
                System.out.println("Both players are in! Starting game start delay (2 seconds)...");
                new Thread(() -> {
                    try {
                        // wait for 2 seconds before sending the GAME_START message,
                        // to give the clients time to process the connection and
                        // show the confirmation message before showing the game start message
                        Thread.sleep(2000);

                        // after the delay, we send a GAME_START message to
                        // both players with the initial board and "the game has started! Player X's turn"
                        GameMessage startMsg = new GameMessage(CommandType.GAME_START,
                                "The game has started! Player " + currentTurn + "'s turn", currentTurn);
                        // send the same message to both players,
                        // so they are synchronized and know the game has started
                        sendToAll(startMsg);

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.err.println("Game start delay was interrupted.");
                    }
                }).start();
            }

        } catch (IOException e) {
            System.err.println("Error sending message to client during connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleMakeMove(GameMessage msg, ConnectionToClient client) {
        int row = msg.getRow();
        int col = msg.getCol();
        // playerSign is the sign of the player who made the move (X or O),
        // which is sent by the client in the message. This is important for validation and updating the board.
        char playerSign = msg.getPlayerSign();

        // check if the current player is the one who should play now, we ignore the move
        // and send back an update with an error message "it's not your turn!"
        if (playerSign != currentTurn) {
            sendToIndividual(client, new GameMessage(CommandType.ERROR, "it's not your turn!", playerSign));
            return;
        }

        // if the cell is not empty, we ignore the move and send back an update
        if (board[row][col] != ' ') {
            sendToIndividual(client, new GameMessage(CommandType.ERROR, "Cell is not empty!", playerSign));
            return;
        }

        // if the move is valid, we update the board with the player's sign
        board[row][col] = playerSign;
        System.out.println("Player " + playerSign + " placed on [" + row + "][" + col + "]");

        // if the move caused a win, we send a game over message
        // to both players with the final board and a message
        if (checkWin(playerSign)) {
            System.out.println("Game Over! Player " + playerSign + " won!");
            lastWinner = playerSign;
            GameMessage updateMsg = new GameMessage(CommandType.UPDATE_BOARD, row, col, playerSign);
            updateMsg.setTextMessage("END");
            sendToAll(updateMsg);

            // we start a new thread to delay the message sending,
            // so the clients have time to update the board and
            // show the winning move before showing the game over message
            new Thread(() -> {
                try {
                    Thread.sleep(400);
                    GameMessage winMsg = new GameMessage(CommandType.GAME_OVER, "Player " + playerSign + " won!", playerSign);
                    sendToAll(winMsg);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            return;
        }

        // check if the move caused a tie (board is full and no winner),
        // we send a game over message to both players with the final board and a message "It's a tie!"
        if (isBoardFull()) {
            System.out.println("Game Over! It's a tie.");
            GameMessage updateMsg = new GameMessage(CommandType.UPDATE_BOARD, row, col, playerSign);
            updateMsg.setTextMessage("END");
            sendToAll(updateMsg);

            // we start a new thread to delay the message sending,
            // so the clients have time to update the board and show
            // the last move before showing It's a tie! message
            new Thread(() -> {
                try {
                    Thread.sleep(400);
                    GameMessage tieMsg = new GameMessage(CommandType.GAME_OVER, "It's a tie!", 'T');
                    sendToAll(tieMsg);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            return;
        }

        // switch the turn to the other player
        currentTurn = (currentTurn == 'X') ? 'O' : 'X';
        GameMessage updateMsg = new GameMessage(CommandType.UPDATE_BOARD, row, col, playerSign);
        sendToAll(updateMsg);
    }

    private void handlePlayerLeave(ConnectionToClient client) {
        System.out.println("A player has left the game.");
        // if 'x' is the one who left -> we send the game over message to O (if still connected)
        if (client == playerX)
        {
            if (playerO != null) {
                lastWinner = 'O';
                GameMessage gameOverMsg = new GameMessage(CommandType.GAME_OVER,
                        "Player X has left the game. Player O wins by default.", 'O');
                sendToAll(gameOverMsg);
            }
        }
        // if 'o' is the one who left -> we send the game over message to X (if still connected)
        else if (client == playerO)
        {
            if (playerX != null) {
                lastWinner = 'X';
                GameMessage gameOverMsg = new GameMessage(CommandType.GAME_OVER,
                        "Player O has left the game. Player X wins by default.", 'X');
                sendToAll(gameOverMsg);
            }
        }
        clearBoard();
        currentTurn = lastWinner;
        System.out.println("Game variables reset. Server is ready for new players.");
    }

    private void handleRestart() {

        // reset the server variables to be ready for a new game when new players connect.
        clearBoard();
        currentTurn = lastWinner;
        // create a new game start message with the initial board and "the game has restarted! Player X's turn"
        GameMessage startNewGameMsg = new GameMessage(CommandType.GAME_START,
                "The game started! Player " + currentTurn + "'s turn", currentTurn
        );

        // send the new game start message to both players,
        // so they are synchronized and know the game has restarted
        sendToAll(startNewGameMsg);
    }

    private boolean checkWin(char sign) {
        // check rows and columns
        for (int i = 0; i < 3; i++) {
            // check row i
            if (board[i][0] == sign && board[i][1] == sign && board[i][2] == sign) {
                return true;
            }
            // check column i
            if (board[0][i] == sign && board[1][i] == sign && board[2][i] == sign) {
                return true;
            }
        }

        // check the main diagonal (from top-left to bottom-right)
        if (board[0][0] == sign && board[1][1] == sign && board[2][2] == sign) {
            return true;
        }

        // check the second-diagonal (from top-right to bottom-left)
        if (board[0][2] == sign && board[1][1] == sign && board[2][0] == sign) {
            return true;
        }

        // if no win condition is met, return false
        return false;
    }
    // this method is called when the server detects that
    // a client has disconnected (closed the window)
    private void handleClientWindowClosed(ConnectionToClient client) {

        if (client == playerX) {
            playerX = null;

            if (playerO != null) {
                // server sends an error message to O
                // that the opponent left, and waits for a new player to connect
                GameMessage msg = new GameMessage(CommandType.ERROR, "Your opponent closed the game. Waiting for a new player...", 'O');
                sendToIndividual(playerO, msg);
            }
        }
        else if (client == playerO) {
            playerO = null;

            if (playerX != null) {
                // server sends an error message to X
                // that the opponent left, and waits for a new player to connect
                GameMessage msg = new GameMessage(CommandType.ERROR, "Your opponent closed the game. Waiting for a new player...", 'X');
                sendToIndividual(playerX, msg);
            }
        }

        clearBoard();
        currentTurn = lastWinner;
    }
    private boolean isBoardFull()
    {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                // if we find any empty cell, the board is not full, so we return false
                if (board[i][j] == ' ') {
                    return false;
                }
            }
        }
        // if we went through the whole board and
        // didn't find any empty space, the board is full
        return true;
    }

    private void clearBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = ' ';
            }
        }
    }
}