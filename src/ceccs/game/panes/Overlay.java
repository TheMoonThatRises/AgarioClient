package ceccs.game.panes;

import ceccs.Client;
import ceccs.game.objects.ui.Player;
import ceccs.network.NetworkHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class Overlay extends BorderPane {

    Label fps;
    Label mass;
    Label ping;

    public Overlay(Player player) {
        super.setPadding(new Insets(10));

        this.fps = new Label("fps:");
        this.mass = new Label("mass:");
        this.ping = new Label("ping:");

        this.mass.setLayoutY(Client.screenHeight);
        this.mass.textProperty().bind(player.massProperty().asString("mass: %.2f"));

        Client.heartbeat.addRoutine(now -> {
            fps.setText(String.format("fps: %.2f", Client.heartbeat.getFramerate()));
            ping.setText(String.format("ping: %.2fms", NetworkHandler.getPing() / 1_000_000.0));
        });

        VBox infographics = new VBox(5, this.fps, this.mass, this.ping);
        HBox mapLayout = new HBox(new Map(player));
        mapLayout.setAlignment(Pos.BASELINE_RIGHT);

        super.setTop(infographics);
        super.setBottom(mapLayout);
    }

}
