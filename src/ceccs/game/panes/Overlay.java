package ceccs.game.panes;

import ceccs.Client;
import ceccs.network.NetworkHandler;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class Overlay extends GridPane {

    Label fps;
    Label mass;
    Label ping;

    public Overlay(ReadOnlyDoubleProperty massProperty) {
        this.fps = new Label("fps:");
        this.mass = new Label("mass:");
        this.ping = new Label("ping:");

        this.mass.setLayoutY(Client.screenHeight);
        this.mass.textProperty().bind(massProperty.asString("mass: %.2f"));

        Client.heartbeat.addRoutine(now -> {
            fps.setText(String.format("fps: %.2f", Client.heartbeat.getFramerate()));
            ping.setText(String.format("ping: %.2fms", NetworkHandler.getPing() / 1_000_000.0));
        });

        super.add(this.fps, 0, 0);
        super.add(this.mass, 0, 1);
        super.add(this.ping, 0, 2);
    }

}
