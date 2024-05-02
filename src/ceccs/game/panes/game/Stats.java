package ceccs.game.panes.game;

import ceccs.Client;
import ceccs.game.objects.ui.Player;
import ceccs.game.roots.GameRoot;
import ceccs.game.roots.LandingRoot;
import ceccs.game.utils.Utilities;
import ceccs.network.NetworkHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class Stats extends VBox {
    final private Label fps;
    final private Label mass;
    final private Label tps;
    final private Label ping;
    final private Label coordinate;
    final private Label serverCode;

    public Stats(Player player) {
        super(5);

        this.fps = new Label("fps:");
        this.mass = new Label("mass:");
        this.tps = new Label("tps:");
        this.ping = new Label("ping:");
        this.coordinate = new Label("coords:");
        this.serverCode = new Label("server code: " + LandingRoot.getConnectedServer());

        this.fps.setFont(Utilities.veraMono);
        this.mass.setFont(Utilities.veraMono);
        this.tps.setFont(Utilities.veraMono);
        this.ping.setFont(Utilities.veraMono);
        this.coordinate.setFont(Utilities.veraMono);
        this.serverCode.setFont(Utilities.veraMono);

        this.mass.textProperty().bind(player.massProperty().asString("mass: %.2f"));
        this.serverCode.setOnMousePressed(_ -> Client.copyToClipboard(serverCode.getText().split("\\s")[2]));

        this.serverCode.setOnMouseEntered(_ -> setCursor(Cursor.HAND));
        this.serverCode.setOnMouseExited(_ -> setCursor(Cursor.DEFAULT));

        GameRoot.heartbeat.addRoutine("overlay", _ -> {
            this.fps.setText(String.format("fps: %.2f", GameRoot.heartbeat.getFramerate()));
            this.tps.setText(String.format("tps: %.2f, %.2f", NetworkHandler.getSocketTps(), NetworkHandler.getServerTps()));
            this.ping.setText(String.format("ping: %.2fms", NetworkHandler.getPing()));
            this.coordinate.setText(
                    String.format(
                            "coords: %d, %d",
                            (int) player.getX(),
                            (int) player.getX()
                    )
            );
        });

        super.getChildren().addAll(fps, mass, tps, ping, coordinate, serverCode);
    }

}
