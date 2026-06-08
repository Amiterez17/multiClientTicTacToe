package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.io.IOException;

public class SecondaryController {

    @FXML
    private TextField hostField;

    @FXML
    private TextField portField;

    @FXML
    private Button connectBtn;

    @FXML
    private Label errorLabel;

    @FXML
    void connectToServer(ActionEvent event) {

        errorLabel.setText("");

        String host = hostField.getText().trim();
        String portStr = portField.getText().trim();

        // check if host or port fields are empty
        if (host.isEmpty() || portStr.isEmpty()) {
            errorLabel.setText("Please enter both Host and Port.");
            return;
        }

        try {
            // convert port string to integer
            int port = Integer.parseInt(portStr);

            SimpleClient client = SimpleClient.getClient();
            client.setHost(host);
            client.setPort(port);

            // change to the primary screen (game board)
            App.setRoot("primary");

        } catch (NumberFormatException e) {
            errorLabel.setText("Port must be a valid number.");
        } catch (IOException e) {
            errorLabel.setText("Failed to switch to the game board screen.");
            e.printStackTrace();
        }
    }
}