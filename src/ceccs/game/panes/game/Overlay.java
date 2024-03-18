package ceccs.game.panes.game;

import ceccs.Client;
import ceccs.game.objects.ui.Player;
import ceccs.game.utilities.Utilities;
import ceccs.network.NetworkHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class Overlay extends BorderPane {

    final private Label fps;
    final private Label mass;
    final private Label ping;
    final private Label coordinate;

    final private Map map;
    final private Leaderboard leaderboard;

    public Overlay(Game game) {
        super.setPadding(new Insets(10));

        this.fps = new Label("fps:");
        this.mass = new Label("mass:");
        this.ping = new Label("ping:");
        this.coordinate = new Label("coords:");

        this.fps.setFont(Utilities.veraMono);
        this.mass.setFont(Utilities.veraMono);
        this.ping.setFont(Utilities.veraMono);
        this.coordinate.setFont(Utilities.veraMono);

        this.map = new Map(game.getSelfPlayer());
        this.leaderboard = new Leaderboard(game);

        this.mass.textProperty().bind(game.getSelfPlayer().massProperty().asString("mass: %.2f"));

        Client.heartbeat.addRoutine(now -> {
            this.fps.setText(String.format("fps: %.2f", Client.heartbeat.getFramerate()));
            this.ping.setText(String.format("ping: %.2fms", NetworkHandler.getPing() / 1_000_000.0));
            this.coordinate.setText(
                String.format(
                    "coords: %d, %d",
                    (int) game.getSelfPlayer().getX(),
                    (int) game.getSelfPlayer().getX()
                )
            );
        });

        VBox infographics = new VBox(5, this.fps, this.mass, this.ping, this.coordinate);
        HBox mapLayout = new HBox(this.map);
        HBox leaderboardLayout = new HBox(this.leaderboard);

        infographics.setAlignment(Pos.TOP_LEFT);
        mapLayout.setAlignment(Pos.BASELINE_RIGHT);
        leaderboardLayout.setAlignment(Pos.TOP_RIGHT);

        super.setTop(infographics);
        super.setBottom(mapLayout);
        super.setTop(leaderboardLayout);
    }

}
