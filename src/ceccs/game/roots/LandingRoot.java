package ceccs.game.roots;

import ceccs.Client;
import ceccs.game.SceneHandler;
import ceccs.game.panes.landing.SettingsCheckBox;
import ceccs.game.utils.AddressCompress;
import ceccs.game.utils.Utilities;
import ceccs.network.NetworkHandler;
import ceccs.network.data.IdentifyPacket;
import ceccs.utils.InternalException;
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
            if (!newValue.isEmpty() && !Character.isLetterOrDigit(newValue.charAt(newValue.length() - 1))) {
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

        Button goButton = new Button("Go");
        goButton.setFont(Utilities.veraMono);
        goButton.setOnAction(_ -> {
            errorLabel.setVisible(false);

            String username = usernameField.getText();
            Pair<String, Integer> address;

            try {
                address = AddressCompress.decodeAddress(serverCode.getText());
            } catch (InternalException exception) {
                errorLabel.setText(exception.getMessage());

                errorLabel.setVisible(true);

                return;
            }

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

        SettingsCheckBox qualityGraphics = new SettingsCheckBox(
                "Quality Graphics",
                "client.settings.performance.graphics.quality",
                "false"
        );

        SettingsCheckBox fpsSetting = new SettingsCheckBox(
                "Show FPS",
                "client.settings.stats.fps",
                "false"
        );
        SettingsCheckBox massSetting = new SettingsCheckBox(
                "Show mass",
                "client.settings.stats.mass",
                "false"
        );
        SettingsCheckBox tpsSetting = new SettingsCheckBox(
                "Show TPS",
                "client.settings.stats.tps",
                "false"
        );
        SettingsCheckBox pingSetting = new SettingsCheckBox(
                "Show ping",
                "client.settings.stats.ping",
                "true"
        );
        SettingsCheckBox coordsSetting = new SettingsCheckBox(
                "Show coordinates",
                "client.settings.stats.coords",
                "false"
        );
        SettingsCheckBox serverCodeSetting = new SettingsCheckBox(
                "Show server code",
                "client.settings.stats.servercode",
                "true"
        );

        SettingsCheckBox leaderboardSetting = new SettingsCheckBox(
                "Show leaderboard",
                "client.settings.misc.leaderboard",
                "true"
        );
        SettingsCheckBox mapSetting = new SettingsCheckBox(
                "Show map",
                "client.settings.misc.map",
                "true"
        );

        SettingsCheckBox usernameSetting = new SettingsCheckBox(
                "Show usernames",
                "client.settings.misc.username",
                "true"
        );

        Label graphicsTitle = new Label("Graphics");
        graphicsTitle.setFont(Utilities.veraMonoBold);

        Label statsTitle = new Label("Stats");
        statsTitle.setFont(Utilities.veraMonoBold);

        Label miscTitle = new Label("Misc");
        miscTitle.setFont(Utilities.veraMonoBold);

        vbox.getChildren().addAll(
                new HBox(5, serverCodeLabel, serverCode),
                new HBox(5, usernameLabel, usernameField),
                graphicsTitle,
                new VBox(5, qualityGraphics),
                statsTitle,
                new HBox(5,
                        new VBox(5, fpsSetting, massSetting, tpsSetting),
                        new VBox(5, pingSetting, coordsSetting, serverCodeSetting)
                ),
                miscTitle,
                new HBox(5,
                        new VBox(5, leaderboardSetting, mapSetting),
                        new VBox(5, usernameSetting)
                ),
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
