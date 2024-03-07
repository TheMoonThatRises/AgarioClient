package ceccs.game.objects.ui;

import ceccs.exceptions.InternalException;
import ceccs.game.objects.BLOB_TYPES;
import ceccs.game.panes.Game;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;

import static ceccs.exceptions.InternalException.checkSafeDivision;
import static ceccs.game.configs.PlayerConfigs.*;
import static ceccs.game.configs.VirusConfigs.virusMass;
import static ceccs.game.utilities.Utilities.*;

public class Player {

    protected static class Cooldowns {
        public long pellet;
        public long split;
        public long merge;

        public Cooldowns() {
            this.pellet = 0;
            this.split = 0;
            this.merge = 0;
        }

        public void updateCooldowns(Cooldowns cooldowns) {
            this.pellet = cooldowns.pellet;
            this.split = cooldowns.split;
            this.merge = cooldowns.merge;
        }
    }

    protected static class PlayerBlob extends Blob {

        final public UUID parentUUID;

        protected double maxVx;
        protected double maxVy;

        protected boolean hasSplitSpeedBoost;

        protected double splitBoostVelocity;

        protected Cooldowns cooldowns;

        protected PlayerBlob physicsUpdate;

        protected ObservableMap<UUID, PlayerBlob> parentMap;

        public PlayerBlob(double x, double y, double vx, double vy, double ax, double ay, double mass, boolean hasSplitSpeedBoost, Paint fill, Game game, UUID parentUUID, UUID uuid, ObservableMap<UUID, PlayerBlob> parentMap) {
            super(x, y, vx, vy, ax, ay, mass, fill, game, uuid, null);

            this.maxVx = 0;
            this.maxVy = 0;

            this.hasSplitSpeedBoost = hasSplitSpeedBoost;
            this.splitBoostVelocity = playerSplitVelocity;

            this.cooldowns = new Cooldowns();

            this.parentUUID = parentUUID;

            this.parentMap = parentMap;
        }

        @Override
        public BLOB_TYPES getType() {
            return BLOB_TYPES.PLAYER;
        }

        @Override
        public void removeFromMap() {
            parentMap.remove(uuid);
        }

        public void positionTick(MouseEvent mouseEvent) {
            Double relX = null;
            Double relY = null;

            if (mouseEvent != null && !mouseEvent.isConsumed()) {
                relX = mouseEvent.getX() - getRelativeX();
                relY = mouseEvent.getY() - getRelativeY();

                maxVx = playerVelocities[closestNumber(playerVelocities, relX / 1000)];
                maxVy = playerVelocities[closestNumber(playerVelocities, relY / 1000)];
            }

            double velScale = calcVelocityModifier(mass.get());

            ax = maxVx < 0
                ? -Math.abs(ax)
                : Math.abs(ax);
            ay = maxVy < 0
                ? -Math.abs(ay)
                : Math.abs(ay);

            vx = (
                Math.abs(vx) < Math.abs(maxVx)
                    ? vx + ax
                    : maxVx
            );
            vy = (
                Math.abs(vy) < Math.abs(maxVy)
                    ? vy + ay
                    : maxVy
            );

            if (hasSplitSpeedBoost && relX != null) {
                splitBoostVelocity -= playerSplitDecay;

                if (splitBoostVelocity <= 0) {
                    hasSplitSpeedBoost = false;
                } else {
                    double delta = Math.atan2(relY, relX);

                    double sVX = splitBoostVelocity * Math.cos(delta);
                    double sVY = splitBoostVelocity * Math.sin(delta);

                    vx += sVX;
                    vy += sVY;
                }
            }

            x += vx * velScale;
            y += vy * velScale;
        }

        @Override
        public void updatePhysicsDataTick(long now) {
            if (this.physicsUpdate != null) {
                maxVx = physicsUpdate.maxVx;
                maxVy = physicsUpdate.maxVy;
                hasSplitSpeedBoost = physicsUpdate.hasSplitSpeedBoost;
                splitBoostVelocity = physicsUpdate.splitBoostVelocity;

                cooldowns.updateCooldowns(physicsUpdate.cooldowns);

                this.physicsUpdate = null;
            }

            super.updatePhysicsDataTick(now);
        }

        public void updatePhysics(PlayerBlob blob) {
            this.physicsUpdate = blob;

            super.updatePhysics(blob);
        }

        public static PlayerBlob fromJSON(JSONObject data, Game game, ObservableMap<UUID, PlayerBlob> parentMap) {
            return new PlayerBlob(
                data.getDouble("x"), data.getDouble("y"), data.getDouble("vx"), data.getDouble("vy"),
                data.getDouble("ax"), data.getDouble("ay"), data.getDouble("mass"),
                data.getBoolean("has_split_speed_boost"), Paint.valueOf(data.getString("fill")),
                game, UUID.fromString(data.getString("parent_uuid")), UUID.fromString(data.getString("uuid")),
                parentMap
            );
        }
    }

    final public UUID uuid;

    protected ObservableMap<UUID, PlayerBlob> playerBlobs;
    protected DoubleProperty totalMass;
    protected NumberBinding massBinding;

    MouseEvent mouseEvent;
    HashMap<KeyCode, Boolean> keyEvents;

    protected Game game;

    protected Player physicsUpdate;

    protected Player(Game game, UUID uuid, JSONArray playerBlobs) {
        this.uuid = uuid;
        this.playerBlobs = FXCollections.observableHashMap();

        this.totalMass = new DoublePropertyBase(0) {
            @Override
            public Object getBean() {
                return Player.this;
            }

            @Override
            public String getName() {
                return "totalMass";
            }
        };

        this.playerBlobs.addListener((MapChangeListener<UUID, PlayerBlob>) change -> {
            totalMass.unbind();
            massBinding = null;

            this.playerBlobs.values().forEach(playerBlob -> {
                if (massBinding == null) {
                    massBinding = playerBlob.massProperty().add(0);
                } else {
                    massBinding = massBinding.add(playerBlob.massProperty());
                }
            });

            totalMass.bind(massBinding);
        });

        for (int i = 0; i < playerBlobs.length(); ++i) {
            PlayerBlob playerBlob = PlayerBlob.fromJSON(playerBlobs.getJSONObject(i), game, this.playerBlobs);
            this.playerBlobs.put(playerBlob.uuid, playerBlob);
        }

        this.mouseEvent = null;
        this.keyEvents = new HashMap<>();

        this.game = game;
    }

    public void removeFromPane() {
        playerBlobs.values().forEach(playerBlob -> {
            if (playerBlob.getParent() != null) {
                game.getChildren().remove(playerBlob);
            }
        });
    }

    public void toFront(boolean spike) {
        playerBlobs.values()
            .stream()
            .filter(blob -> spike
                ? blob.mass.greaterThanOrEqualTo(virusMass).get()
                : blob.mass.lessThan(virusMass).get())
            .forEach(PlayerBlob::toFront);
    }
    public double getX() {
        double numerator = playerBlobs.values().stream().map(blob -> blob.mass.get() * blob.x).reduce(0.0, Double::sum);
        double denominator = playerBlobs.values().stream().map(blob -> blob.mass.get()).reduce(0.0, Double::sum);

        try {
            return checkSafeDivision(numerator, denominator);
        } catch (InternalException exception) {
            exception.printStackTrace();

            System.err.println("failed to get player x");

            return getLegacyX();
        }
    }

    public double getY() {
        double numerator = playerBlobs.values().stream().map(blob -> blob.mass.get() * blob.y).reduce(0.0, Double::sum);
        double denominator = playerBlobs.values().stream().map(blob -> blob.mass.get()).reduce(0.0, Double::sum);

        try {
            return checkSafeDivision(numerator, denominator);
        } catch (InternalException exception) {
            exception.printStackTrace();

            System.err.println("failed to get player y");

            return getLegacyY();
        }
    }

    protected double getLegacyX() {
        return playerBlobs.values().stream().max(Comparator.comparingDouble(b -> b.mass.get())).get().getX();
    }

    protected double getLegacyY() {
        return playerBlobs.values().stream().max(Comparator.comparingDouble(b -> b.mass.get())).get().getY();
    }

    public void positionTick() {
        playerBlobs.values().forEach(playerBlob -> playerBlob.positionTick(mouseEvent));
    }

    public void collisionTick(long time) {
        ArrayList<UUID> uuidList = new ArrayList<>(playerBlobs.keySet());

        for (int i = playerBlobs.size() - 1; i >= 0; --i) {
            PlayerBlob playerBlob = playerBlobs.get(uuidList.get(i));

            playerBlob.collisionTick();

            for (int j = playerBlobs.size() - 1; j >= 0; --j) {
                PlayerBlob checkBlob = playerBlobs.get(uuidList.get(i));

                if (playerBlob.uuid != checkBlob.uuid) {
                    if (
                        playerBlob.cooldowns.merge < time &&
                        checkBlob.cooldowns.merge < time
                    ) {
                        if (checkCollision(playerBlob, checkBlob)) {
                            playerBlob.mass.set(playerBlob.mass.get() + checkBlob.mass.get());

                            checkBlob.removeFromPane();
                            playerBlobs.remove(uuidList.get(j));
                        }
                    } else if (
                        checkTouch(playerBlob, checkBlob) &&
                        checkBlob.mass.get() <= playerBlob.mass.get()
                    ) {
                        double[] pos = repositionBlob(playerBlob, checkBlob);

                        checkBlob.setX(pos[0]);
                        checkBlob.setY(pos[1]);
                    }
                }
            }
        }
    }

    public void animationTick() {
        playerBlobs.values().forEach(PlayerBlob::animationTick);
    }

    public ReadOnlyDoubleProperty massProperty() {
        return totalMass;
    }

    public void updateMouseEvent(MouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
    }

    public void updatePhysicsDataTick(long now) {
        if (physicsUpdate != null) {
            ArrayList<UUID> uuidList = new ArrayList<>(playerBlobs.keySet());
            for (int i = playerBlobs.size() - 1; i >= 0; --i) {
                playerBlobs.get(uuidList.get(i)).updatePhysicsDataTick(now);
            }

            physicsUpdate.playerBlobs.values().forEach(blob -> {
                if (!playerBlobs.containsKey(blob.uuid)) {
                    playerBlobs.put(blob.uuid, blob);
                }
            });

            physicsUpdate = null;
        } else {
            removeFromPane();
            game.players.remove(uuid);
        }
    }

    public void updatePhysics(Player player) {
        player.playerBlobs.values().forEach(blob -> {
            if (playerBlobs.containsKey(blob.uuid)) {
                playerBlobs.get(blob.uuid).updatePhysics(blob);
            }
        });

        physicsUpdate = player;
    }

    public static Player fromJSON(JSONObject data, Game game) {
        JSONArray playerBlobsData = data.getJSONArray("player_blobs");

        return new Player(game, UUID.fromString(data.getString("uuid")), playerBlobsData);
    }

}
