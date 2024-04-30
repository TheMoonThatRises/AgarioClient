package ceccs.game;

import ceccs.game.roots.GameRoot;
import ceccs.game.roots.LandingRoot;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
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

        Scene parentScene = stage.getScene();

        switch (scene) {
            case LANDING -> {
                clearEvents();

                this.gameRoot.stop();

                parentScene.setRoot(landingRoot);
            }
            case GAME -> {
                parentScene.setRoot(gameRoot);

                setEvents(gameRoot);

                this.gameRoot.start();
            }
            case END -> {
                clearEvents();

                this.gameRoot.stop();
            }
        }
    }

    private void setEvents(Pane pane) {
        Scene parentScene = stage.getScene();

        parentScene.setOnKeyPressed(pane.getOnKeyPressed());
        parentScene.setOnKeyReleased(pane.getOnKeyReleased());
        parentScene.setOnMouseMoved(pane.getOnMouseMoved());
        parentScene.setOnMouseExited(pane.getOnMouseExited());
        parentScene.setOnMouseEntered(pane.getOnMouseEntered());
        parentScene.setOnScroll(pane.getOnScroll());
    }

    private void clearEvents() {
        Scene parentScene = stage.getScene();

        parentScene.setOnKeyPressed(_ -> {
        });
        parentScene.setOnKeyReleased(_ -> {
        });
        parentScene.setOnMouseMoved(_ -> {
        });
        parentScene.setOnMouseExited(_ -> {
        });
        parentScene.setOnMouseEntered(_ -> {
        });
        parentScene.setOnScroll(_ -> {
        });
    }

    public enum SCENES {
        LANDING, GAME, END
    }

}
