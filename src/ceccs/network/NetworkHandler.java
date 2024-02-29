package ceccs.network;

import ceccs.Client;
import ceccs.game.panes.Game;
import ceccs.network.data.*;
import ceccs.network.utils.GZip;
import javafx.application.Platform;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class NetworkHandler {

    final private static int pingInterval = 1_000;

    final private InetSocketAddress serverSocket;

    final private DatagramSocket clientSocket;

    final private Thread serverThread;

    final private TimerTask pingTask;
    final private Timer pingTimer;

    final private IdentifyPacket identifyPacket;

    final private Game game;

    private long lastPing;

    private double ping;

    public NetworkHandler(String hostname, int port, Game game) throws SocketException {
        this.serverSocket = new InetSocketAddress(hostname, port);

        this.identifyPacket = new IdentifyPacket(Client.screenWidth, Client.screenHeight);

        this.clientSocket = new DatagramSocket();
        this.clientSocket.setSoTimeout(pingInterval * 2);

        this.game = game;

        this.lastPing = System.nanoTime();
        this.ping = 0;

        this.pingTimer = new Timer("server_ping_thread");

        this.serverThread = new Thread(() -> {
            while (true) {
                byte[] buf = new byte[65534];

                DatagramPacket inPacket = new DatagramPacket(buf, buf.length);

                try {
                    clientSocket.receive(inPacket);

                    handleIncomingPacket(inPacket);
                } catch (IOException exception) {
                    System.err.println("failed receiving incoming packet: " + exception);

                    if (Client.registerPacket == null) {
                        System.out.println("attempting to re-identify");

                        identify();
                    }
                }
            }
        });
        this.pingTask = new TimerTask() {
            @Override
            public void run() {
                handleWritePacket(OP_CODES.CLIENT_PING);

                lastPing = System.nanoTime();
            }
        };
    }

    private void handleIncomingPacket(DatagramPacket packet) {
        try {
            String received = new String(GZip.decompress(packet.getData()));
            NetworkPacket networkPacket = NetworkPacket.fromString(received);

            Optional<OP_CODES> opcode = OP_CODES.fromValue(networkPacket.op);

            opcode.ifPresentOrElse(op -> {
                if (op != OP_CODES.SERVER_IDENTIFY_OK && Client.registerPacket == null) {
                    System.err.println("received improper packet from server");

                    return;
                }

                switch (op) {
                    case SERVER_IDENTIFY_OK -> Client.registerPacket = RegisterPacket.fromJSON(networkPacket.data);
                    case SERVER_PONG -> ping = (System.nanoTime() - lastPing) / 1_000_000.0;
                    case SERVER_GAME_STATE -> game.updateFromGameData(networkPacket.data);
                    case SERVER_TERMINATE -> {
                        Platform.exit();
                        System.exit(0);
                    }
                    default -> System.out.println("unhandled op code: " + op);
                }
            }, () -> System.err.println("received unknown opcode: " + networkPacket.op));
        } catch (IOException exception) {
            System.err.println("failed to decompress packet: " + exception);
        }
    }

    public void identify() {
        if (Client.registerPacket != null) {
            System.err.println("already identified to server");

            return;
        }

        handleWritePacket(OP_CODES.CLIENT_IDENTIFY, identifyPacket.toJSON());
    }

    public void terminate() {
        if (Client.registerPacket == null) {
            System.err.println("cannot terminate without identifying first");

            return;
        }

        handleWritePacket(OP_CODES.CLIENT_TERMINATE);
    }

    private void handleWritePacket(OP_CODES op, JSONObject data) {
        NetworkPacket networkPacket = new NetworkPacket(op, data);

        try {
            byte[] byteData = GZip.compress(networkPacket.toJSON().toString().getBytes());

            DatagramPacket packet = new DatagramPacket(byteData, byteData.length, serverSocket);

            try {
                clientSocket.send(packet);
            } catch (IOException exception) {
                System.err.println("failed to send packet to server: " + exception);
            }
        } catch (IOException exception) {
            System.err.println("failed to compress packet: " + exception);
        }
    }

    private void handleWritePacket(OP_CODES op) {
        handleWritePacket(op, new JSONObject("{}"));
    }

    public void start() {
        serverThread.start();
        pingTimer.scheduleAtFixedRate(pingTask, pingInterval * 2, pingInterval);
    }

    public void writeMousePacket(double x, double y) {
        handleWritePacket(OP_CODES.CLIENT_MOUSE_UPDATE, new MousePacket(x, y).toJSON());
    }

    public void writeKeyPacket(int keycode, boolean pressed) {
        handleWritePacket(OP_CODES.CLIENT_KEYBOARD_UPDATE, new KeyPacket(keycode, pressed).toJSON());
    }

    public double getPing() {
        return ping;
    }

}
