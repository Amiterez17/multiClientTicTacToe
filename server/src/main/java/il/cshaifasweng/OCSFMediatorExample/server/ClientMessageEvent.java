package il.cshaifasweng.OCSFMediatorExample.server;
import il.cshaifasweng.OCSFMediatorExample.entities.GameMessage;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public class ClientMessageEvent {
    private final GameMessage message;
    private final ConnectionToClient client;

    public ClientMessageEvent(GameMessage message, ConnectionToClient client) {
        this.message = message;
        this.client = client;
    }
    public GameMessage getMessage() { return message; }
    public ConnectionToClient getClient() { return client; }
}