package ceccs.game.panes.game;

import ceccs.Client;
import ceccs.game.objects.Camera;
import ceccs.game.objects.ui.*;
import ceccs.game.roots.GameRoot;
import ceccs.network.utils.CustomID;
import ceccs.utils.InternalException;
import javafx.scene.layout.Pane;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Game extends Pane {

    final public static double gridSpacing = 10;
    final public Camera camera;
    final public ArrayList<GridItem> gridItems;

    final public ConcurrentHashMap<CustomID, Player> players;
    final public ConcurrentHashMap<CustomID, Food> foods;
    final public ConcurrentHashMap<CustomID, Pellet> pellets;
    final public ConcurrentHashMap<CustomID, Virus> viruses;

    final private AtomicBoolean hasPhysicsUpdate;

    final private AtomicBoolean updatingPhysics;

    public Game() {
        this.setWidth(Client.screenWidth);
        this.setHeight(Client.screenHeight);

        this.setPrefSize(Client.screenWidth, Client.screenHeight);

        this.gridItems = new ArrayList<>();

        this.foods = new ConcurrentHashMap<>();
        this.pellets = new ConcurrentHashMap<>();
        this.viruses = new ConcurrentHashMap<>();
        this.players = new ConcurrentHashMap<>();

        this.camera = new Camera();

        this.hasPhysicsUpdate = new AtomicBoolean(false);
        this.updatingPhysics = new AtomicBoolean(false);

        GameRoot.heartbeat.addRoutine("game", now -> {
            if (GameRoot.registerPacket == null) {
                if (!players.isEmpty()) {
                    // TODO: overlay and camera are not updated
                    players.values().forEach(Player::removeFromPane);
                    players.clear();
                }

                return;
            }

            Player thisPlayer = getSelfPlayer();

            if (thisPlayer == null) {
                System.err.println("unable to find current player");

                return;
            }

            camera.smoothCameraTick();

            if (hasPhysicsUpdate.get()) {
                players.values().forEach(Player::updatePhysicsDataTick);
                pellets.values().forEach(Blob::updatePhysicsDataTick);
                viruses.values().forEach(Blob::updatePhysicsDataTick);
                foods.values().forEach(Blob::updatePhysicsDataTick);

                hasPhysicsUpdate.set(false);
            }

            players.values().forEach(Player::positionTick);
            pellets.values().forEach(Pellet::positionTick);

            camera.setX(thisPlayer.getX() - Client.screenWidth / camera.getCameraScale() / 2);
            camera.setY(thisPlayer.getY() - Client.screenHeight / camera.getCameraScale() / 2);

            viruses.values().forEach(Virus::collisionTick);
            pellets.values().forEach(Pellet::collisionTick);
            foods.values().forEach(Food::collisionTick);

            players.values().forEach(player -> player.collisionTick(now));

            viruses.values().forEach(Virus::animationTick);
            pellets.values().forEach(Pellet::animationTick);
            foods.values().forEach(Food::animationTick);
            gridItems.forEach(GridItem::animationTick);

            players.values().forEach(Player::animationTick);

            pellets.values().forEach(Pellet::toFront);
            players.values().forEach(player -> player.toFront(false));
            viruses.values().forEach(Virus::toFront);
            players.values().forEach(player -> player.toFront(true));
        });
    }

    public void load() {
        try {
            camera.setMass(this.getSelfPlayer().massProperty());
        } catch (InternalException exception) {
            System.err.println("player mass is zero?");

            throw new RuntimeException(exception);
        }

        for (double x = 0; x <= GameRoot.registerPacket.width(); x += gridSpacing) {
            GridItem line = new GridItem(x, 0, x, GameRoot.registerPacket.height(), this);

            gridItems.add(line);

            getChildren().add(line);
        }

        for (double y = 0; y <= GameRoot.registerPacket.height(); y += gridSpacing) {
            GridItem line = new GridItem(0, y, GameRoot.registerPacket.width(), y, this);

            gridItems.add(line);

            getChildren().add(line);
        }
    }

    public Player getSelfPlayer() {
        return players.get(GameRoot.registerPacket.playerUUID());
    }

    public void updateFromGameData(JSONObject data) {
        if (updatingPhysics.get()) {
            System.err.println("server sending packets faster than processing");
            return;
        }

        updatingPhysics.set(true);

        // load foods
        JSONArray foodArray = data.getJSONArray("foods");

        for (int i = 0; i < foodArray.length(); ++i) {
            Food food = Food.fromBlob(Blob.fromJSON(foodArray.getJSONObject(i), this, foods));

            try {
                if (foods.containsKey(food.uuid)) {
                    foods.get(food.uuid).updatePhysics(food);
                } else {
                    foods.put(food.uuid, food);
                }
            } catch (NullPointerException exception) {
                exception.printStackTrace();

                System.err.println("concurrent issue looping through food");
            }
        }

        // update pellets
        JSONArray pelletArray = data.getJSONArray("pellets");

        for (int i = 0; i < pelletArray.length(); ++i) {
            Pellet pellet = Pellet.fromBlob(Blob.fromJSON(pelletArray.getJSONObject(i), this, pellets));

            try {
                if (pellets.containsKey(pellet.uuid)) {
                    pellets.get(pellet.uuid).updatePhysics(pellet);
                } else {
                    pellets.put(pellet.uuid, pellet);
                }
            } catch (NullPointerException exception) {
                exception.printStackTrace();

                System.err.println("concurrent issue looping through pellet");
            }
        }

        // update viruses
        JSONArray virusArray = data.getJSONArray("viruses");

        for (int i = 0; i < virusArray.length(); ++i) {
            Virus virus = Virus.fromBlob(Blob.fromJSON(virusArray.getJSONObject(i), this, viruses));

            try {
                if (viruses.containsKey(virus.uuid)) {
                    viruses.get(virus.uuid).updatePhysics(virus);
                } else {
                    viruses.put(virus.uuid, virus);
                }
            } catch (NullPointerException exception) {
                exception.printStackTrace();

                System.err.println("concurrent issue when looping through virus");
            }
        }

        // update player data
        JSONArray playerArray = data.getJSONArray("players");

        for (int i = 0; i < playerArray.length(); ++i) {
            Player player = Player.fromJSON(playerArray.getJSONObject(i), this);

            try {
                if (players.containsKey(player.uuid)) {
                    players.get(player.uuid).updatePhysics(player);
                } else {
                    players.put(player.uuid, player);
                }
            } catch (NullPointerException exception) {
                exception.printStackTrace();

                System.err.println("concurrent issue when looping through player");
            }
        }

        updatingPhysics.set(false);
        hasPhysicsUpdate.set(true);
    }

    public void forceReload() {
        getChildren().clear();

        gridItems.clear();
        players.clear();
        pellets.clear();
        viruses.clear();

        while (getSelfPlayer() == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException exception) {
                throw new RuntimeException(exception);
            }
        }

        load();
    }

}
