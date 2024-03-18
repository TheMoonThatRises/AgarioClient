package ceccs.game.panes.game;

import ceccs.Client;
import ceccs.game.objects.ui.Player;
import ceccs.game.utilities.Utilities;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class Leaderboard extends VBox {

    final private Label[] topTen;

    final private double leaderboardDim;

    public Leaderboard(Game game) {
        super(10);

        super.setBackground(Background.fill(Utilities.opacityColor(Color.DARKGREY, 0.8)));
        super.setPadding(new Insets(10));

        this.leaderboardDim = Client.screenHeight * 2 / 9;

        super.setMaxWidth(leaderboardDim);
        super.setMinWidth(leaderboardDim);

        this.topTen = new Label[10];

        for (int i = 0; i < 10; ++i) {
            this.topTen[i] = new Label(String.format("%d. ", i + 1));
            this.topTen[i].setFont(Utilities.veraMono);
            this.topTen[i].setTextFill(Color.WHITE);
        }

        Client.heartbeat.addRoutine(now -> {
            ArrayList<Player> topTenPlayers = new ArrayList<> (
                game.players.values()
                    .stream()
                    .sorted((pl1, pl2) -> (int) pl2.massProperty().subtract(pl1.massProperty()).get())
                    .toList()
            );

            for (int i = 0; i < 10; ++i) {
                if (i >= topTenPlayers.size()) {
                    topTen[i].setText(String.format("%d. ", i + 1));

                    topTen[i].setTextFill(Color.WHITE);
                } else {
                    Player player = topTenPlayers.get(i);

                    topTen[i].setText(String.format("%d. %s", i + 1, player.getUsername()));

                    topTen[i].setTextFill(player.uuid == game.getSelfPlayer().uuid ? Color.PINK : Color.WHITE);
                }
            }
        });

        super.getChildren().addAll(topTen);
    }

}
