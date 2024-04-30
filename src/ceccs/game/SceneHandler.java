package ceccs.game;

import ceccs.game.roots.GameRoot;
import ceccs.game.roots.LandingRoot;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneHandler {

    final private Stage stage;
    final private GameRoot gameRoot;
    final private LandingRoot landingRoot;

    public SceneHandler(Stage stage) throws IOException {
        this.stage = stage;

        this.gameRoot = new GameRoot();

        this.landingRoot = new LandingRoot();

        Scene scene = new Scene(this.landingRoot);

        this.stage.setScene(scene);
    }

    public void setScene(SCENES scene) {
        stage.setFullScreen(true);
        stage.setMaximized(true);

        switch (scene) {
            case LANDING -> {
                this.gameRoot.stop();

                stage.getScene().setRoot(landingRoot);
            }
            case GAME -> {
                stage.getScene().setRoot(gameRoot);

                this.gameRoot.start();
            }
            case END -> {
                this.gameRoot.stop();
            }
        }
    }

    public enum SCENES {
        LANDING, GAME, END
    }

}
