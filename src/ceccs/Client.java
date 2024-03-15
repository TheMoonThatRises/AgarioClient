package ceccs;

import ceccs.game.objects.Heartbeat;
import ceccs.game.panes.game.Overlay;
import ceccs.game.panes.game.Game;
import ceccs.network.NetworkHandler;
import ceccs.network.data.RegisterPacket;
import ceccs.utils.Configurations;
import ceccs.utils.InternalException;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.net.InetSocketAddress;
import java.util.Scanner;

public class Client extends Application {

    final public static Rectangle2D screen;

    final public static double screenWidth;
    final public static double screenHeight;

    final public static StackPane main;
    private static Game game;

    final public static Heartbeat heartbeat;

    public static RegisterPacket registerPacket = null;

    static private NetworkHandler networkHandler;

    static private boolean didMouseExit;

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
        InetSocketAddress server = getServer();

        Configurations.shared.setProperty("server.ip", server.getHostString());
        Configurations.shared.setProperty("server.port", String.valueOf(server.getPort()));

        game = new Game();
        networkHandler = new NetworkHandler(server, game);

        System.out.println("attempting to connect to " + server.getAddress() + ":" + server.getPort());

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
        Overlay overlay = new Overlay(game.getSelfPlayer());

        main.getChildren().addAll(game, overlay);

        Scene scene = new Scene(main);
        primaryStage.setScene(scene);

        primaryStage.show();

        Thread.sleep(500);

        didMouseExit = false;

        scene.setOnMouseMoved(event -> {
            if (didMouseExit) {
                return;
            }

            networkHandler.writeMousePacket(event.getX(), event.getY());

            if (registerPacket != null && game.getSelfPlayer() != null) {
                game.getSelfPlayer().updateMouseEvent(event);
            }
        });
        scene.setOnScroll(event -> {
            if (didMouseExit) {
                return;
            }

            try {
                game.camera.updateScrollWheel(event);
            } catch (InternalException exception) {
                exception.printStackTrace();

                System.err.println("player mass is zero?");
            }
        });
        scene.setOnKeyPressed(event -> {
            if (didMouseExit) {
                return;
            }

            networkHandler.writeKeyPacket(event.getCode().getCode(), true);
        });
        scene.setOnKeyReleased(event -> {
            if (didMouseExit) {
                return;
            }

            networkHandler.writeKeyPacket(event.getCode().getCode(), false);
        });

        scene.setOnMouseExited(event -> didMouseExit = true);
        scene.setOnMouseEntered(event -> didMouseExit = false);

        heartbeat.start();

        System.out.println("loading scene");
    }

    private InetSocketAddress getServer() {
        String serverIp = "";
        Integer serverPort = null;

        Scanner scanner = new Scanner(System.in);

        while (serverIp.isEmpty()) {
            System.out.print("\nenter server ip: ");

            serverIp = scanner.nextLine().trim();
        }

        while (serverPort == null) {
            System.out.print("\nenter server port: ");

            try {
                serverPort = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException exception) {
                System.err.println("failed to parse server port input: " + exception);
            }
        }

        System.out.println();

        return new InetSocketAddress(serverIp, serverPort);
    }

}
