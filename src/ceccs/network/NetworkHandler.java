package ceccs.network;

import ceccs.Client;
import ceccs.game.panes.game.Game;
import ceccs.network.data.*;
import ceccs.network.utils.GZip;
import ceccs.utils.InternalPathFinder;
import javafx.application.Platform;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class NetworkHandler {

    final private InternalPathFinder networkLogger;
    final private long networkSampleTime;
    private long lastWrite;

    final private static int pingInterval = 1_000;

    final private InetSocketAddress serverSocket;

    final private DatagramSocket clientSocket;

    final private Thread serverThread;

    final private TimerTask pingTask;
    final private Timer pingTimer;

    final private IdentifyPacket identifyPacket;

    final private Game game;

    private long lastPing;

    private static long ping;

    private long timeoutSleep;

    public NetworkHandler(IdentifyPacket identifyPacket, InetSocketAddress server, Game game) throws IOException {
        this.serverSocket = server;

        this.identifyPacket = identifyPacket;

        this.clientSocket = new DatagramSocket();
        this.clientSocket.setSoTimeout(pingInterval * 2);
        this.clientSocket.setTrafficClass(0x10);

        this.game = game;

        this.timeoutSleep = 1_000;
        this.lastPing = System.nanoTime();

        this.pingTimer = new Timer("server_ping_thread");

        this.networkLogger = new InternalPathFinder(true, "logs", "network-samples.log");
        this.networkSampleTime = 15_000_000_000L;
        this.lastWrite = 0;

        this.serverThread = new Thread(() -> {
            while (true) {
                byte[] buf = new byte[65534];

                DatagramPacket inPacket = new DatagramPacket(buf, buf.length);

                try {
                    clientSocket.receive(inPacket);

                    new Thread(() -> handleIncomingPacket(inPacket)).start();
                } catch (IOException exception) {
                    exception.printStackTrace();

                    System.err.println("failed receiving incoming packet");

                    if (Client.registerPacket == null) {
                        System.out.printf("re-identifying in %d seconds\n", timeoutSleep / 1_000);

                        try {
                            Thread.sleep(timeoutSleep);

                            timeoutSleep *= 2;
                        } catch (InterruptedException rException) {
                            rException.printStackTrace();

                            System.err.println("failed to sleep");
                        }

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

            if (lastWrite + networkSampleTime < System.nanoTime() || lastWrite == 0) {
                networkLogger.writeToFile(received + "\n", true);

                lastWrite = System.nanoTime();
            }

            Optional<OP_CODES> opcode = OP_CODES.fromValue(networkPacket.op);

            opcode.ifPresentOrElse(op -> {
                if (op != OP_CODES.SERVER_IDENTIFY_OK && Client.registerPacket == null) {
                    System.err.println("received improper packet from server");

                    return;
                }

                switch (op) {
                    case SERVER_IDENTIFY_OK -> {
                        Client.registerPacket = RegisterPacket.fromJSON(networkPacket.data);

                        System.out.println("successfully connected to server");
                        timeoutSleep = 1_000;

                        pingTimer.scheduleAtFixedRate(pingTask, pingInterval, pingInterval);
                    }
                    case SERVER_PONG -> ping = System.nanoTime() - lastPing;
                    case SERVER_GAME_STATE -> game.updateFromGameData(networkPacket.data);
                    case SERVER_TERMINATE -> {
                        Platform.exit();
                        System.exit(0);
                    }
                    case CLIENT_UNIDENTIFIED_ERROR -> {
                        System.err.println("client unidentified error received: server restarted?");

                        Client.registerPacket = null;

                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException exception) {
                            System.err.println("failed to stall thread: " + exception);
                        }

                        identify();
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
        if (op != OP_CODES.CLIENT_IDENTIFY && Client.registerPacket == null) {
            System.err.println("register packet is null");

            return;
        }

        NetworkPacket networkPacket = new NetworkPacket(op, data);

        try {
            byte[] byteData = GZip.compress(networkPacket.toJSON().toString().getBytes());

            DatagramPacket packet = new DatagramPacket(byteData, byteData.length, serverSocket);

            try {
                clientSocket.send(packet);
            } catch (IOException exception) {
                exception.printStackTrace();

                System.err.println("failed to send packet to server");
            }
        } catch (IOException exception) {
            exception.printStackTrace();

            System.err.println("failed to compress packet");
        }
    }

    private void handleWritePacket(OP_CODES op) {
        handleWritePacket(op, new JSONObject("{}"));
    }

    public void start() {
        serverThread.start();
    }

    public void writeMousePacket(double x, double y) {
        handleWritePacket(OP_CODES.CLIENT_MOUSE_UPDATE, new MousePacket(x, y).toJSON());
    }

    public void writeKeyPacket(int keycode, boolean pressed) {
        handleWritePacket(OP_CODES.CLIENT_KEYBOARD_UPDATE, new KeyPacket(keycode, pressed).toJSON());
    }

    public static long getPing() {
        return ping;
    }

}
