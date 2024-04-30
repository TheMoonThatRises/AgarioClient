package ceccs.game.roots;

import ceccs.Client;
import ceccs.game.SceneHandler;
import ceccs.game.utils.Utilities;
import ceccs.network.NetworkHandler;
import ceccs.network.data.IdentifyPacket;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.net.InetSocketAddress;

public class LandingRoot extends HBox {

    private static InetSocketAddress server = null;
    private static IdentifyPacket identifyPacket = null;

    final private TextField ipField;
    final private TextField portField;

    final private TextField usernameField;

    final private Label errorLabel;

    public LandingRoot() {
        VBox vbox = new VBox();

        this.ipField = new TextField(Client.configs.getProperty("server.ip", "127.0.0.1"));
        this.ipField.setFont(Utilities.veraMono);

        this.portField = new TextField(Client.configs.getProperty("server.port", "2341"));
        this.portField.setFont(Utilities.veraMono);

        this.usernameField = new TextField(Client.configs.getProperty("client.player.username", "Unamed blob"));
        this.usernameField.setFont(Utilities.veraMono);

        this.errorLabel = new Label();
        this.errorLabel.setFont(Utilities.veraMono);
        this.errorLabel.setTextFill(Color.RED);
        this.errorLabel.setVisible(false);

        Label ipLabel = new Label("IP:");
        ipLabel.setFont(Utilities.veraMono);

        Label portLabel = new Label("Port:");
        portLabel.setFont(Utilities.veraMono);

        Label usernameLabel = new Label("Username:");
        usernameLabel.setFont(Utilities.veraMono);

        Button goButton = new Button("Go");
        goButton.setFont(Utilities.veraMono);
        goButton.setOnAction(_ -> {
            errorLabel.setVisible(false);

            String username = usernameField.getText();
            String ip = ipField.getText();
            int port = Integer.parseInt(portField.getText());

            Client.configs.setProperty("client.player.username", username);

            Client.configs.setProperty("server.ip", ip);
            Client.configs.setProperty("server.port", String.valueOf(port));

            server = new InetSocketAddress(ip, port);

            Pair<Boolean, String> serverVerification = NetworkHandler.verifyServer(server);

            if (!serverVerification.getKey()) {
                errorLabel.setText(serverVerification.getValue());

                errorLabel.setVisible(true);

                return;
            }

            identifyPacket = new IdentifyPacket(username, Client.screenWidth, Client.screenHeight);

            Client.getSceneHandler().setScene(SceneHandler.SCENES.GAME);
        });

        vbox.getChildren().addAll(
                new HBox(5, ipLabel, ipField),
                new HBox(5, portLabel, portField),
                new HBox(5, usernameLabel, usernameField),
                new HBox(5, goButton, errorLabel)
        );

        super.setAlignment(Pos.CENTER);
        super.getChildren().add(vbox);
    }

    public static InetSocketAddress getServer() {
        return server;
    }

    public static IdentifyPacket getIdentifyPacket() {
        return identifyPacket;
    }

}
