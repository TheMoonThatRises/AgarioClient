package ceccs.game.panes;

import ceccs.Client;
import ceccs.game.objects.Camera;
import ceccs.game.objects.ui.*;
import javafx.scene.layout.Pane;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Game extends Pane {

    final public Camera camera;

    final public static double gridSpacing = 10;
    final public ArrayList<GridItem> gridItems;

    final public ConcurrentHashMap<UUID, Player> players;
    final public ConcurrentHashMap<UUID, Food> foods;
    final public ConcurrentHashMap<UUID, Pellet> pellets;
    final public ConcurrentHashMap<UUID, Virus> viruses;

    private AtomicBoolean hasPhysicsUpdate;

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

        Client.heartbeat.addRoutine(now -> {
            if (Client.registerPacket == null) {
                if (!players.isEmpty()) {
                    // TODO: overlay and camera are not updated
                    players.values().forEach(Player::removeFromPane);
                    players.clear();
                }

                return;
            }

            Player thisPlayer = getSelfPlayer();

            if (thisPlayer == null) {
                return;
            }

            camera.smoothCameraTick();

            if (hasPhysicsUpdate.get()) {
                players.values().forEach(player -> player.updatePhysicsDataTick(now));
                pellets.values().forEach(pellet -> pellet.updatePhysicsDataTick(now));
                viruses.values().forEach(virus -> virus.updatePhysicsDataTick(now));
                foods.values().forEach(food -> food.updatePhysicsDataTick(now));
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
        camera.setMass(this.getSelfPlayer().massProperty());

        for (double x = 0; x <= Client.registerPacket.width(); x += gridSpacing) {
            GridItem line = new GridItem(x, 0, x, Client.registerPacket.height(), this);

            gridItems.add(line);

            getChildren().add(line);
        }

        for (double y = 0; y <= Client.registerPacket.height(); y += gridSpacing) {
            GridItem line = new GridItem(0, y, Client.registerPacket.width(), y, this);

            gridItems.add(line);

            getChildren().add(line);
        }
    }

    public Player getSelfPlayer() {
        return players.get(Client.registerPacket.playerUUID());
    }

    public void updateFromGameData(JSONObject data) {
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
                System.err.println("concurrent issue: " + exception);
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
                System.err.println("concurrent issue: " + exception);
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
                System.err.println("concurrent issue: " + exception);
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
                System.err.println("concurrent issue: " + exception);
            }
        }

        hasPhysicsUpdate.set(true);
    }

}
