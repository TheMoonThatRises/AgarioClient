package ceccs.game.roots;

import ceccs.game.objects.Heartbeat;
import ceccs.game.panes.game.Game;
import ceccs.game.panes.game.Overlay;
import ceccs.network.NetworkHandler;
import ceccs.network.data.RegisterPacket;
import ceccs.utils.InternalException;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class GameRoot extends StackPane {

    final public static Heartbeat heartbeat = new Heartbeat();
    public static RegisterPacket registerPacket = null;
    final private NetworkHandler networkHandler;
    final private Game game;
    private boolean didMouseExit;
    private boolean isEnabled;

    public GameRoot() throws IOException {
        this.didMouseExit = false;
        this.isEnabled = false;

        this.game = new Game();
        this.networkHandler = new NetworkHandler(game);

        Runtime.getRuntime().addShutdownHook(new Thread(networkHandler::terminate));

        super.setOnMouseMoved(event -> {
            if (didMouseExit || !isEnabled) {
                return;
            }

            networkHandler.writeMousePacket(event.getX(), event.getY());

            if (registerPacket != null && game.getSelfPlayer() != null) {
                game.getSelfPlayer().updateMouseEvent(event);
            }
        });
        super.setOnScroll(event -> {
            if (didMouseExit || !isEnabled) {
                return;
            }

            try {
                game.camera.updateScrollWheel(event);
            } catch (InternalException exception) {
                exception.printStackTrace();

                System.err.println("player mass is zero?");
            }
        });
        super.setOnKeyPressed(event -> {
            if (didMouseExit || !isEnabled) {
                return;
            }

            if (event.getCode() == KeyCode.R) {
                game.forceReload();
            }

            networkHandler.writeKeyPacket(event.getCode().getCode(), true);
        });
        super.setOnKeyReleased(event -> {
            if (didMouseExit || !isEnabled) {
                return;
            }

            networkHandler.writeKeyPacket(event.getCode().getCode(), false);
        });

        super.setOnMouseExited(_ -> didMouseExit = true);
        super.setOnMouseEntered(_ -> didMouseExit = false);
    }

    public void start() {
        System.out.println(
                "attempting to connect to " +
                        LandingRoot.getServer().getAddress() + ":" +
                        LandingRoot.getServer().getPort()
        );

        networkHandler.identify();
        networkHandler.start();

        System.out.println("waiting for server identify");

        while (registerPacket == null) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException exception) {
                exception.printStackTrace();

                System.err.println("failed to sleep: " + exception);
            }
        }

        System.out.println("server successful identification");

        System.out.println("waiting for server population");

        while (!game.players.containsKey(registerPacket.playerUUID())) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException exception) {
                exception.printStackTrace();

                System.err.println("failed to sleep: " + exception);
            }
        }

        System.out.println("creating overlay");
        Overlay overlay = new Overlay(game);

        this.getChildren().addAll(game, overlay);

        System.out.println("creating game");

        game.load();
        heartbeat.start();

        this.isEnabled = true;
    }

    public void stop() {
        this.isEnabled = false;

        heartbeat.stop();

        this.getChildren().clear();

        networkHandler.terminate();

        networkHandler.stop();
    }

}
