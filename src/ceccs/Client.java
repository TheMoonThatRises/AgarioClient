package ceccs;

import ceccs.game.SceneHandler;
import ceccs.utils.Configurations;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Client extends Application {

    final public static Rectangle2D screen;

    final public static double screenWidth;
    final public static double screenHeight;
    final public static Configurations configs = Configurations.shared;
    private static SceneHandler sceneHandler;

    static {
        screen = Screen.getPrimary().getVisualBounds();
        screenWidth = screen.getWidth();
        screenHeight = screen.getHeight();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static SceneHandler getSceneHandler() {
        return sceneHandler;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        primaryStage.setOnCloseRequest(_ -> System.exit(0));

        primaryStage.setX(screen.getMinX());
        primaryStage.setY(screen.getMinY());

        primaryStage.setWidth(screenWidth);
        primaryStage.setHeight(screenHeight);

        primaryStage.setTitle("Agar.io");
        primaryStage.setResizable(false);

        sceneHandler = new SceneHandler(primaryStage);

        primaryStage.show();
    }

}
