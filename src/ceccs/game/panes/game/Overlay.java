package ceccs.game.panes.game;

import ceccs.Client;
import ceccs.game.objects.ui.Player;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;

public class Overlay extends BorderPane {

    final private Stats stats;
    final private Map map;
    final private Leaderboard leaderboard;

    public Overlay(Game game) {
        super.setPadding(new Insets(10));

        Player player = game.getSelfPlayer();

        this.stats = new Stats(player);
        this.map = new Map(player);
        this.leaderboard = new Leaderboard(game);

        BorderPane topRow = new BorderPane();

        topRow.setLeft(this.stats);


        if (Boolean.parseBoolean(Client.configs.getProperty("client.settings.misc.leaderboard"))) {
            topRow.setRight(this.leaderboard);
        }

        BorderPane bottomRow = new BorderPane();

        if (Boolean.parseBoolean(Client.configs.getProperty("client.settings.misc.map"))) {
            bottomRow.setRight(this.map);
        }

        super.setTop(topRow);
        super.setBottom(bottomRow);
    }

}
