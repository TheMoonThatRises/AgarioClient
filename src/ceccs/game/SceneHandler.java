package ceccs.game;

import ceccs.game.scenes.GameScene;
import ceccs.game.scenes.LandingScene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneHandler {

    final private Stage stage;
    final private GameScene gameScene;
    final private LandingScene landingScene;

    public SceneHandler(Stage stage) throws IOException {
        this.stage = stage;

        StackPane gameSceneMain = new StackPane();
        this.gameScene = new GameScene(gameSceneMain);

        StackPane landingSceneMain = new StackPane();
        this.landingScene = new LandingScene(landingSceneMain);

        this.setScene(SCENES.LANDING);
    }

    public void setScene(SCENES scene) {
        stage.setFullScreen(true);
        stage.setMaximized(true);

        switch (scene) {
            case LANDING -> {
                this.gameScene.stop();

                stage.setScene(landingScene);
            }
            case GAME -> {
                stage.setScene(this.gameScene);

                this.gameScene.start();
            }
            case END -> {
                this.gameScene.stop();
            }
        }
    }

    public enum SCENES {
        LANDING, GAME, END
    }

}
