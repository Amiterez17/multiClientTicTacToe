package il.cshaifasweng.OCSFMediatorExample.server;
import il.cshaifasweng.OCSFMediatorExample.entities.GameMessage;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import org.greenrobot.eventbus.EventBus;
import java.io.IOException;
import il.cshaifasweng.OCSFMediatorExample.entities.Warning;
public class SimpleServer extends AbstractServer {

	private ServerGameController gameController;
	private static SimpleServer instance = null;

	public SimpleServer(int port) {
		super(port);
		// creating the game controller and subscribing it to the
		// EventBus so it can receive events from clients
		this.gameController = new ServerGameController();
		instance = this;
	}

	public static SimpleServer getInstance() {
		return instance;
	}

	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {

		// if the message is a GameMessage,
		// we post it to the EventBus for the ServerGameController to handle
		if (msg instanceof GameMessage) {
			GameMessage gameMsg = (GameMessage) msg;
			// posting the message to the EventBus,
			// so the ServerGameController can react to it
			EventBus.getDefault().post(new ClientMessageEvent(gameMsg, client));
			return;
		}

		// if the message is not a GameMessage,
		// we check if it's a string command for testing purposes
		String msgString = msg.toString();

		if (msgString.startsWith("#warning")) {
			Warning warning = new Warning("Warning from server!");
			try {
				client.sendToClient(warning);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void clientDisconnected(ConnectionToClient client) {
		System.out.println("Client closed the window and disconnected.");
		GameMessage disconnectMsg = new GameMessage(GameMessage.CommandType.DISCONNECT);
		EventBus.getDefault().post(new ClientMessageEvent(disconnectMsg, client));
	}
}