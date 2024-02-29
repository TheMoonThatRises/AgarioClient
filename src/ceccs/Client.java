package ceccs;

import ceccs.game.objects.Heartbeat;
import ceccs.game.panes.Game;
import ceccs.game.panes.Overlay;
import ceccs.network.NetworkHandler;
import ceccs.network.data.RegisterPacket;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Client extends Application {

    final public static Rectangle2D screen;

    final public static double screenWidth;
    final public static double screenHeight;

    final public static StackPane main;
    private static Game game;

    final public static Heartbeat heartbeat;

    public static RegisterPacket registerPacket = null;

    static private NetworkHandler networkHandler;

    static {
        screen = Screen.getPrimary().getVisualBounds();
        screenWidth = screen.getWidth();
        screenHeight = screen.getHeight();

        main = new StackPane();

        heartbeat = new Heartbeat();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        game = new Game();
        networkHandler = new NetworkHandler("192.168.86.207", 2351, game);

        networkHandler.start();
        networkHandler.identify();

        System.out.println("waiting for server identify");

        while (registerPacket == null) {
            Thread.sleep(500);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> networkHandler.terminate()));
        primaryStage.setOnCloseRequest(event -> System.exit(0));

        System.out.println("server successful identification");

        primaryStage.setX(screen.getMinX());
        primaryStage.setY(screen.getMinY());

        primaryStage.setWidth(screenWidth);
        primaryStage.setHeight(screenHeight);

        primaryStage.setFullScreen(true);

        primaryStage.setTitle("Agar.io");
        primaryStage.setResizable(false);

        System.out.println("waiting for server population");

        while (!game.players.containsKey(registerPacket.playerUUID())) {
            Thread.sleep(500);
        }

        System.out.println("server successful population");

        System.out.println("creating game");
        game.load();
        System.out.println("creating overlay");
        Overlay overlay = new Overlay(game.getSelfPlayer().massProperty());
        main.getChildren().addAll(game, overlay);

        Scene scene = new Scene(main);
        primaryStage.setScene(scene);

        primaryStage.show();

        Thread.sleep(500);

        scene.setOnMouseMoved(event -> {
            networkHandler.writeMousePacket(event.getX(), event.getY());
            game.getSelfPlayer().updateMouseEvent(event);
        });
        scene.setOnKeyPressed(event -> networkHandler.writeKeyPacket(event.getCode().getCode(), true));
        scene.setOnKeyReleased(event -> networkHandler.writeKeyPacket(event.getCode().getCode(), false));

        heartbeat.start();

        System.out.println("loading scene");
    }

}
