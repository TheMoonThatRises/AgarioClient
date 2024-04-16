package ceccs.game.panes.game;

import ceccs.Client;
import ceccs.game.objects.ui.Player;
import ceccs.game.utils.Utilities;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Map extends Pane {

    final private double mapDim;
    final private Circle location;

    public Map(Player player) {
        super.setBackground(Background.fill(Utilities.opacityColor(Color.DARKGREY, 0.8)));

        this.mapDim = Client.screenHeight / 6;

        super.setMaxSize(mapDim, mapDim);
        super.setMinSize(mapDim, mapDim);

        this.location = new Circle(mapDim / 100, Color.WHITE);

        Client.heartbeat.addRoutine(_ -> {
            this.location.setCenterX(player.getX() / Client.registerPacket.width() * mapDim);
            this.location.setCenterY(player.getY() / Client.registerPacket.height() * mapDim);
        });

        super.getChildren().add(this.location);
    }

}
