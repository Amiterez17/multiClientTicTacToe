package il.cshaifasweng.OCSFMediatorExample.client;
import il.cshaifasweng.OCSFMediatorExample.entities.GameMessage;
import java.io.IOException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

public class PrimaryController {
	// This variable will track whose turn it is
	// ('X' or 'O'), it will be updated
	private char currentTurn = 'X'; // Default to 'X', will be updated by server message
	// This variable will hold the player's assigned
	// sign ('X' or 'O') once received from the server
	private char mySign;

	@FXML
	private Button btn00;

	@FXML
	private Button btn01;

	@FXML
	private Button btn02;

	@FXML
	private Button btn10;

	@FXML
	private Button btn11;

	@FXML
	private Button btn12;

	@FXML
	private Button btn20;

	@FXML
	private Button btn21;

	@FXML
	private Button btn22;

	@FXML
	private Label gameLabel;

	@FXML
	private GridPane grid;

	@FXML
	private Button newGameBtn;

	@FXML
	private Button LeaveBtn;

	@FXML
	private Label statusLabel;

	@FXML
	void initialize()
	{
		SimpleClient.setController(this);
		try {
			GameMessage connectMsg = new GameMessage(GameMessage.CommandType.CONNECT);
			SimpleClient.getClient().sendToServer(connectMsg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			statusLabel.setText("Failed to connect to server.");
		}
		statusLabel.setText("Connecting to the server...");
		grid.setDisable(true);
		if (newGameBtn != null) newGameBtn.setDisable(true);
		if (LeaveBtn != null) LeaveBtn.setDisable(true);
	}

	@FXML
	void sendWarning(ActionEvent event) {
		try {
			SimpleClient.getClient().sendToServer("#warning");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@FXML
	void onCellClicked(ActionEvent event)
	{
		//	If the player hasn't received their assigned sign from the server yet, ignore clicks on the grid
		if (mySign == '\0') return;
		// get the button that was clicked
		Button clickedButton = (Button) event.getSource();
		Integer rowIndex = GridPane.getRowIndex(clickedButton);
		Integer colIndex = GridPane.getColumnIndex(clickedButton);

		int row = (rowIndex != null) ? rowIndex : 0;
		int col = (colIndex != null) ? colIndex : 0;

		// Check if it's the player's turn
		if (mySign != currentTurn) {
			statusLabel.setText("It's not your turn!");
			return;
		}
		// Check if the clicked cell is already occupied
		if (!clickedButton.getText().isEmpty()) {
			statusLabel.setText("This cell is already occupied!");
			return;
		}

		try {
			GameMessage moveMessage = new GameMessage(GameMessage.CommandType.MAKE_MOVE,row,col,mySign);
			SimpleClient.getClient().sendToServer(moveMessage);

		} catch (IOException e) {
			System.err.println("Failed to send move to server: " + e.getMessage());
			statusLabel.setText("Failed to send move to server.");
			e.printStackTrace();
		}
	}

	@FXML
	void onNewGameClicked(ActionEvent event)
	{
		// Close the client connection and exit the application
		GameMessage moveMessage = new GameMessage(GameMessage.CommandType.RESTART);
		try {
			SimpleClient.getClient().sendToServer(moveMessage);
			statusLabel.setText("Requesting restart from server...");
		} catch (IOException e) {
			System.err.println("Failed to send new game message to server: " + e.getMessage());
			statusLabel.setText("Failed to request restart from server.");
		}
	}

	@FXML
	void onLeaveClicked(ActionEvent event) {
		try {
			// Send a LEAVE message to the server to indicate that the player is leaving the game
			GameMessage leaveMessage = new GameMessage(GameMessage.CommandType.LEAVE);
			SimpleClient.getClient().sendToServer(leaveMessage);
			statusLabel.setText("You left the game.");
		} catch (IOException e) {
			System.err.println("Failed to send leave message to server: " + e.getMessage());
			statusLabel.setText("Failed to process leave request.");
			e.printStackTrace();
		}
	}
	// This method will be called when the client receives a CONNECT response from the server
	public void handleConnectResponse(char assignedSign, String message) {

		this.mySign = assignedSign;
		statusLabel.setText(message);
	}

	public void updateSingleCell(int row, int col, char sign, String status) {

		Button[][] buttonMap = {{btn00, btn01, btn02}, {btn10, btn11, btn12}, {btn20, btn21, btn22}};
		// Update the text of the button at the specified row and column
		buttonMap[row][col].setText(String.valueOf(sign));

		// Update the current turn to the other player
		this.currentTurn = (sign == 'X') ? 'O' : 'X';
		if (status == null || !status.equals("END")) {
			statusLabel.setText("Turn of player: " + this.currentTurn);
		}
	}

	public void handleGameOver(String message, char winnerSign)
	{
		statusLabel.setText(message);
		grid.setDisable(true);
		if (newGameBtn != null) newGameBtn.setDisable(false);
		if (LeaveBtn != null) LeaveBtn.setDisable(true);
	}
	public void handleGameStart(char startingTurn, String message) {

		// Set the current turn to the starting player as indicated by the server
		this.currentTurn = startingTurn;
		// Update the status label to indicate the game has started and whose turn it is
		statusLabel.setText(message);
		Button[] boardButtons = {btn00, btn01, btn02, btn10, btn11, btn12, btn20, btn21, btn22};
		for (Button btn : boardButtons) {
			if (btn != null) {
				btn.setText(""); // clear the text of each button to reset the board for the new game
			}
		}
		grid.setDisable(false); // enable the grid for the new game
		if (newGameBtn != null) newGameBtn.setDisable(true);
		if (LeaveBtn != null) LeaveBtn.setDisable(false);
	}

	// This method will be called when the client receives an ERROR message from the server
	public void showError(String errorMessage, char sign) {
		// Update the status label
		if (errorMessage != null && errorMessage.equals("Game is full!")) {
			// if the error message indicates that the game is full,
			// we set the status label to indicate that the player is a spectator
			statusLabel.setText("Game is full!");
			grid.setDisable(true); // disable the grid for spectators since they cannot make moves
			if (newGameBtn != null) newGameBtn.setDisable(true);
			if (LeaveBtn != null) LeaveBtn.setDisable(true);
		}
		else if (errorMessage != null && errorMessage.contains("closed the game")) {
			statusLabel.setText(errorMessage); //  Your opponent closed the game...

			grid.setDisable(true); // lock the grid since the game is closed and no more moves can be made
			if (newGameBtn != null) newGameBtn.setDisable(true);
			if (LeaveBtn != null) LeaveBtn.setDisable(true);
		}
		else {
			statusLabel.setText(errorMessage + " (Player " + sign + ")");
		}
	}
}