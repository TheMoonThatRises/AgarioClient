package ceccs;

import ceccs.game.SceneHandler;
import ceccs.utils.Configurations;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Client extends Application {

    final public static Rectangle2D screen;
    final public static double screenWidth;
    final public static double screenHeight;
    final public static Configurations configs = Configurations.shared;
    final private static Clipboard clipboard = Clipboard.getSystemClipboard();
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

    public static void copyToClipboard(String text) {
        ClipboardContent clipboardContent = new ClipboardContent();

        clipboardContent.putString(text);

        clipboard.setContent(clipboardContent);
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
