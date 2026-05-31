package il.cshaifasweng.OCSFMediatorExample.entities;
import java.io.Serializable;

public class GameMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    // types of commands that can be sent between client and server
    public enum CommandType {
        CONNECT,        // the client wants to connect to the server
        MAKE_MOVE,      // the client reports a move (clicking on a square)
        RESTART,        // the client requests a new game
        LEAVE,          // the client wants to leave the game
        DISCONNECT,     // the client wants to disconnect from the server

        ERROR,          // the server indicates: an error occurred (invalid move, not your turn)
        GAME_START,     // the server indicates: both players are here, the game can starts
        UPDATE_BOARD,   // the server indicates: the board has been updated
        GAME_OVER       // the server indicates: the game is over and who is the winner (or if it's a tie)
    }

    private CommandType commandType;
    private int row = -1;
    private int col = -1;
    private char playerSign;         // 'X' or 'O' - constant during the game, assigned by the server at the start of the game
    private String textMessage;      // text message to display in the interface ("Your turn!")

    // client to server - RESTART, LEAVE, CONNECT
    public GameMessage(CommandType commandType) {
        this.commandType = commandType;
    }

    // server to client - CONNECT, ERROR, GAME_START, GAME_OVER
    public GameMessage(CommandType commandType,String textMessage ,char playerSign) {
        this.commandType = commandType;
        this.textMessage = textMessage;
        this.playerSign = playerSign;
    }

    // client to server - MAKE_MOVE
    // client to server - UPDATE_BOARD
    public GameMessage(CommandType commandType, int row, int col, char playerSign) {
        this.commandType = commandType;
        this.row = row;
        this.col = col;
        this.playerSign = playerSign;
    }

    public CommandType getCommandType() { return commandType; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public char getPlayerSign() { return playerSign; }
    public String getTextMessage() { return textMessage; }
    public void setTextMessage(String textMessage) { this.textMessage = textMessage;
    }
}