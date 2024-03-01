package ceccs.game.panes;

import ceccs.Client;
import ceccs.game.objects.ui.Player;
import ceccs.network.NetworkHandler;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class Overlay extends GridPane {

    private final long[] frameTimes = new long[100];
    private int frameTimeIndex = 0;
    private boolean arrayFilled = false;

    Label fps;
    Label mass;
    Label ping;

    public Overlay(NetworkHandler networkHandler, ReadOnlyDoubleProperty massProperty) {
        this.fps = new Label("fps:");
        this.mass = new Label("mass:");
        this.ping = new Label("ping:");

        this.mass.setLayoutY(Client.screenHeight);
        this.mass.textProperty().bind(massProperty.asString("mass: %.2f"));

        Client.heartbeat.addRoutine(now -> {
            long oldFrameTime = frameTimes[frameTimeIndex] ;
            frameTimes[frameTimeIndex] = now ;
            frameTimeIndex = (frameTimeIndex + 1) % frameTimes.length ;
            if (frameTimeIndex == 0) {
                arrayFilled = true ;
            }

            if (arrayFilled) {
                long elapsedNanos = now - oldFrameTime ;
                long elapsedNanosPerFrame = elapsedNanos / frameTimes.length ;
                double frameRate = 1_000_000_000.0 / elapsedNanosPerFrame ;
                fps.setText(String.format("fps: %.2f", frameRate));
            }

            ping.setText(String.format("ping: %.2fms", networkHandler.getPing()));
        });

        super.add(this.fps, 0, 0);
        super.add(this.mass, 0, 1);
        super.add(this.ping, 0, 2);
    }

}
