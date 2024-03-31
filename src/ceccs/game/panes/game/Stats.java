package ceccs.game.panes.game;

import ceccs.Client;
import ceccs.game.objects.ui.Player;
import ceccs.game.utilities.Utilities;
import ceccs.network.NetworkHandler;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class Stats extends VBox {
    final private Label fps;
    final private Label mass;
    final private Label ping;
    final private Label coordinate;

    public Stats(Player player) {
        super(5);

        this.fps = new Label("fps:");
        this.mass = new Label("mass:");
        this.ping = new Label("ping:");
        this.coordinate = new Label("coords:");

        this.fps.setFont(Utilities.veraMono);
        this.mass.setFont(Utilities.veraMono);
        this.ping.setFont(Utilities.veraMono);
        this.coordinate.setFont(Utilities.veraMono);

        this.mass.textProperty().bind(player.massProperty().asString("mass: %.2f"));

        Client.heartbeat.addRoutine(_ -> {
            this.fps.setText(String.format("fps: %.2f", Client.heartbeat.getFramerate()));
            this.ping.setText(String.format("ping: %.2fms", NetworkHandler.getPing() / 1_000_000.0));
            this.coordinate.setText(
                    String.format(
                            "coords: %d, %d",
                            (int) player.getX(),
                            (int) player.getX()
                    )
            );
        });

        super.getChildren().addAll(fps, mass, ping, coordinate);
    }

}
