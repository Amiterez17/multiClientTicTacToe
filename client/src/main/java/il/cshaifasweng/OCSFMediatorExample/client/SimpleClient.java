package il.cshaifasweng.OCSFMediatorExample.client;
import il.cshaifasweng.OCSFMediatorExample.entities.GameMessage;
import il.cshaifasweng.OCSFMediatorExample.entities.GameMessage.CommandType;
import javafx.application.Platform;
import org.greenrobot.eventbus.EventBus;

import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;
import il.cshaifasweng.OCSFMediatorExample.entities.Warning;

public class SimpleClient extends AbstractClient {
	
	private static SimpleClient client = null;
	private static volatile PrimaryController controller;
	private SimpleClient(String host, int port) {
		super(host, port);
	}

	public static void setController(PrimaryController primaryController) {
		controller = primaryController;
	}
	@Override
	protected void handleMessageFromServer(Object msg) {
		if (msg.getClass().equals(Warning.class)) {
			EventBus.getDefault().post(new WarningEvent((Warning) msg));
		}
		else{
			// unpacking the message from the server
			if (msg instanceof GameMessage) {
				GameMessage gameMsg = (GameMessage) msg;

				// unpacking the relevant fields from the
				// GameMessage object (depending on the command type, not all fields will be used)
				switch (gameMsg.getCommandType())
				{
					// unpacking only the player's sign and the text message
					case CONNECT:
						Platform.runLater(() -> {
							if (controller != null) {
								controller.handleConnectResponse(gameMsg.getPlayerSign(), gameMsg.getTextMessage());
							}
						});
						break;

					case GAME_START:
						Platform.runLater(() -> {
							if (controller != null) {
								controller.handleGameStart(gameMsg.getPlayerSign(), gameMsg.getTextMessage());
							}
						});
						break;

					case GAME_OVER:
						Platform.runLater(() -> {
							if (controller != null) {
								controller.handleGameOver(gameMsg.getTextMessage(), gameMsg.getPlayerSign());
							}
						});
						break;

                    // unpacking the row, column and player's sign
					// of the move that was just made
					case UPDATE_BOARD:

						int row = gameMsg.getRow();
						int col = gameMsg.getCol();
						char sign = gameMsg.getPlayerSign();
						String status = gameMsg.getTextMessage();
						Platform.runLater(() -> {
							if (controller != null) {
								controller.updateSingleCell(row, col, sign, status);
							}
						});
						break;

					case ERROR:
						Platform.runLater(() -> {
							if (controller != null) {
								controller.showError(gameMsg.getTextMessage(),gameMsg.getPlayerSign());
							}
						});
						break;
				}
			}
		}
	}
	
	public static SimpleClient getClient() {
		if (client == null) {
			client = new SimpleClient("localhost", 3000);
		}
		return client;
	}

	@Override
	protected void connectionClosed()
	{
		System.out.println("Connection to server was closed.");
	}

	@Override
	protected void connectionException(Exception exception) {
		System.err.println("An error occurred with the server connection:");
		exception.printStackTrace();
	}
}