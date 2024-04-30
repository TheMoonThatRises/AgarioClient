package ceccs.game.scenes;

import ceccs.Client;
import ceccs.game.SceneHandler;
import ceccs.game.utils.Utilities;
import ceccs.network.NetworkHandler;
import ceccs.network.data.IdentifyPacket;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.net.InetSocketAddress;

public class LandingScene extends Scene {

    private static InetSocketAddress server = null;
    private static IdentifyPacket identifyPacket = null;

    final private TextField ipField;
    final private TextField portField;

    final private TextField usernameField;

    final private Label errorLabel;

    public LandingScene(Pane root) {
        super(root);

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

            if (!NetworkHandler.verifyServer(server)) {
                errorLabel.setText("failed to reach server");

                errorLabel.setVisible(true);

                return;
            }

            identifyPacket = new IdentifyPacket(username, Client.screenWidth, Client.screenHeight);

            Client.getSceneHandler().setScene(SceneHandler.SCENES.GAME);
        });

        vbox.getChildren().addAll(
                new HBox(ipLabel, ipField),
                new HBox(portLabel, portField),
                new HBox(usernameLabel, usernameField),
                new HBox(goButton, errorLabel)
        );

        root.getChildren().add(vbox);
        StackPane.setAlignment(vbox, Pos.CENTER);
    }

    public static InetSocketAddress getServer() {
        return server;
    }

    public static IdentifyPacket getIdentifyPacket() {
        return identifyPacket;
    }

}
