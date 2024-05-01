package ceccs.game.roots;

import ceccs.Client;
import ceccs.game.SceneHandler;
import ceccs.game.utils.AddressCompress;
import ceccs.game.utils.Utilities;
import ceccs.network.NetworkHandler;
import ceccs.network.data.IdentifyPacket;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
    private static String connectedServer = null;

    final private TextField serverCode;

    final private TextField usernameField;

    final private Label errorLabel;

    public LandingRoot() {
        VBox vbox = new VBox(10);

        String defaultIp = Client.configs.getProperty("server.ip", "127.0.0.1");
        String defaultPort = Client.configs.getProperty("server.port", "2341");

        this.serverCode = new TextField(AddressCompress.encodeAddress(defaultIp, defaultPort));
        this.serverCode.setFont(Utilities.veraMono);

        this.serverCode.textProperty().addListener((_, oldValue, newValue) -> {
            if (!Character.isLetterOrDigit(newValue.charAt(newValue.length() - 1))) {
                this.serverCode.setText(oldValue);
            }
        });

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

        Label serverCodeLabel = new Label("Server Code:");
        serverCodeLabel.setFont(Utilities.veraMono);

        Label usernameLabel = new Label("Username:");
        usernameLabel.setFont(Utilities.veraMono);

        CheckBox qualityGraphics = new CheckBox("Quality Graphics");
        qualityGraphics.setSelected(
                Boolean.parseBoolean(
                    Client.configs.getProperty("client.settings.performance.graphics.quality", "false")
                )
        );
        qualityGraphics.selectedProperty().addListener((_, _, newValue) ->
                Client.configs.setProperty("client.settings.performance.graphics.quality", newValue.toString())
        );

        Button goButton = new Button("Go");
        goButton.setFont(Utilities.veraMono);
        goButton.setOnAction(_ -> {
            errorLabel.setVisible(false);

            String username = usernameField.getText();
            Pair<String, Integer> address = AddressCompress.decodeAddress(serverCode.getText());

            Client.configs.setProperty("client.player.username", username);

            Client.configs.setProperty("server.ip", address.getKey());
            Client.configs.setProperty("server.port", address.getValue().toString());

            server = new InetSocketAddress(address.getKey(), address.getValue());

            Pair<Boolean, String> serverVerification = NetworkHandler.verifyServer(server);

            if (!serverVerification.getKey()) {
                errorLabel.setText(serverVerification.getValue());

                errorLabel.setVisible(true);

                return;
            }

            identifyPacket = new IdentifyPacket(username, Client.screenWidth, Client.screenHeight);

            connectedServer = serverCode.getText();

            Client.getSceneHandler().setScene(SceneHandler.SCENES.GAME);
        });

        vbox.getChildren().addAll(
                new HBox(5, serverCodeLabel, serverCode),
                new HBox(5, usernameLabel, usernameField),
                new VBox(5, qualityGraphics),
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

    public static String getConnectedServer() {
        return connectedServer;
    }

}
