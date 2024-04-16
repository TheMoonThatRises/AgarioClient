package ceccs.game.panes.game;

import ceccs.Client;
import ceccs.game.utils.Utilities;
import ceccs.network.NetworkHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.json.JSONArray;
import org.json.JSONObject;

public class Leaderboard extends VBox {

    final private Label[] topTen;
    final private Label yourPos;

    final private Game game;

    private int prevHash = 0;

    public Leaderboard(Game game) {
        super(10);

        super.setBackground(Background.fill(Utilities.opacityColor(Color.DARKGREY, 0.8)));
        super.setPadding(new Insets(10));

        this.game = game;

        double leaderboardDim = Client.screenHeight * 2 / 11;

        this.yourPos = new Label("");
        this.yourPos.setFont(Utilities.veraMono);
        this.yourPos.setTextFill(Color.PINK);

        super.setMaxWidth(leaderboardDim);
        super.setMinWidth(leaderboardDim);

        this.topTen = new Label[10];

        for (int i = 0; i < 10; ++i) {
            this.topTen[i] = new Label("");
            this.topTen[i].setFont(Utilities.veraMono);
            this.topTen[i].setTextFill(Color.BLACK);
        }

        Client.heartbeat.addRoutine(_ -> {
            JSONObject leaderboard = NetworkHandler.getLeaderboard();

            if (leaderboard != null && prevHash != leaderboard.hashCode()) {
                prevHash = leaderboard.hashCode();

                JSONArray topPlayers = leaderboard.getJSONArray("leaderboard");
                boolean isInTop = false;

                for (int i = 0; i < 10; ++i) {
                    if (i >= topPlayers.length()) {
                        topTen[i].setText("");
                    } else {
                        JSONObject player = topPlayers.getJSONObject(i);

                        this.topTen[i].setText(String.format("%d. %s", i + 1, player.getString("username")));

                        if (player.getString("player_uuid").equals(this.game.getSelfPlayer().uuid.toString())) {
                            isInTop = true;
                            topTen[i].setTextFill(Color.PINK);
                        } else {
                            topTen[i].setTextFill(Color.BLACK);
                        }
                    }
                }

                if (!isInTop) {
                    yourPos.setText("Your Position: " + leaderboard.getInt("position") + 1);

                    if (yourPos.getParent() == null) {
                        getChildren().add(yourPos);
                    }
                } else if (yourPos.getParent() != null) {
                    getChildren().remove(yourPos);
                }
            }
        });

        super.getChildren().addAll(topTen);
    }

}
