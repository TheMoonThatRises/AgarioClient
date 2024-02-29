package ceccs.game.objects.ui;

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

import static ceccs.game.configs.PlayerConfigs.*;
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

        public PlayerBlob(double x, double y, double vx, double vy, double ax, double ay, double mass, boolean hasSplitSpeedBoost, Paint fill, Game game, UUID parentUUID, UUID uuid) {
            super(x, y, vx, vy, ax, ay, mass, fill, game, uuid, null);

            this.maxVx = 0;
            this.maxVy = 0;

            this.hasSplitSpeedBoost = hasSplitSpeedBoost;
            this.splitBoostVelocity = playerSplitVelocity;

            this.cooldowns = new Cooldowns();

            this.parentUUID = parentUUID;
        }

        public PlayerBlob(double x, double y, double vx, double vy, double mass, Paint fill, Game game, UUID uuid, UUID parentUUID) {
            this(x, y, vx, vy, playerMouseAcc, playerMouseAcc, mass, false, fill, game, uuid, parentUUID);
        }

        public PlayerBlob(double x, double y, double mass, boolean hasSplitSpeedBoost, Paint fill, Game game, UUID uuid, UUID parentUUID) {
            this(x, y, 0, 0, playerMouseAcc, playerMouseAcc, mass, hasSplitSpeedBoost, fill, game, uuid, parentUUID);
        }

        @Override
        public BLOB_TYPES getType() {
            return BLOB_TYPES.PLAYER;
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

        public void updatePhysicsDataTick(long now) {
            this.maxVx = physicsUpdate.maxVx;
            this.maxVy = physicsUpdate.maxVy;
            this.hasSplitSpeedBoost = physicsUpdate.hasSplitSpeedBoost;
            this.splitBoostVelocity = physicsUpdate.splitBoostVelocity;

            cooldowns.updateCooldowns(physicsUpdate.cooldowns);

            super.updatePhysicsDataTick(now);
        }

        public void updatePhysics(PlayerBlob blob) {
            this.physicsUpdate = blob;

            super.updatePhysics(blob);
        }

        public static PlayerBlob fromJSON(JSONObject data, Game game) {
            return new PlayerBlob(
                data.getDouble("x"), data.getDouble("y"), data.getDouble("vx"), data.getDouble("vy"),
                data.getDouble("ax"), data.getDouble("ay"), data.getDouble("mass"),
                data.getBoolean("has_split_speed_boost"), Paint.valueOf(data.getString("fill")),
                game, UUID.fromString(data.getString("uuid")), UUID.fromString(data.getString("parent_uuid"))
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
    protected long lastPhysicsUpdate;

    public Player(double x, double y, double vx, double vy, double mass, Paint fill, Game game, UUID uuid
    ) {
        this.uuid = uuid;
        this.playerBlobs = FXCollections.observableHashMap();

        this.totalMass = new DoublePropertyBase(0.0) {
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

        UUID childUUID = UUID.randomUUID();
        this.playerBlobs.put(childUUID, new PlayerBlob(x, y, vx, vy, mass, fill, game, childUUID, uuid));

        this.mouseEvent = null;
        this.keyEvents = new HashMap<>();

        this.game = game;
        this.lastPhysicsUpdate = 0;
    }

    protected Player(Game game, UUID uuid, HashMap<UUID, PlayerBlob> playerBlobs) {
        this.uuid = uuid;
        this.playerBlobs = FXCollections.observableHashMap();

        this.totalMass = new DoublePropertyBase(playerBlobs.values().stream().mapToDouble(blob -> blob.mass.get()).sum()) {
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

        this.playerBlobs.putAll(playerBlobs);

        this.mouseEvent = null;
        this.keyEvents = new HashMap<>();

        this.game = game;
        this.lastPhysicsUpdate = 0;
    }

    public void addToPane() {
        playerBlobs.values().forEach(playerBlob -> {
            if (playerBlob.getParent() == null) {
                game.getChildren().add(playerBlob);
            }
        });
    }

    public void removeFromPane() {
        playerBlobs.values().forEach(playerBlob -> {
            if (playerBlob.getParent() != null) {
                game.getChildren().remove(playerBlob);
            }
        });
    }

    public void toFront() {
        playerBlobs.values().forEach(PlayerBlob::toFront);
    }

    public double getX() {
        return playerBlobs.values().stream().max(Comparator.comparingDouble(b -> b.mass.get())).get().getX();
//        return
//            playerBlobs.stream().map(blob -> blob.mass.get() * blob.x).reduce(0.0, Double::sum) /
//            playerBlobs.stream().map(blob -> blob.mass.get()).reduce(0.0, Double::sum);
    }

    public double getY() {
        return playerBlobs.values().stream().max(Comparator.comparingDouble(b -> b.mass.get())).get().getY();
//        return
//            playerBlobs.stream().map(blob -> blob.mass.get() * blob.y).reduce(0.0, Double::sum) /
//            playerBlobs.stream().map(blob -> blob.mass.get()).reduce(0.0, Double::sum);
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
                        checkBlob.mass.get() < playerBlob.mass.get()
                    ) {
                        double[] pos = repositionBlob(playerBlob, checkBlob);

                        checkBlob.setX(pos[0]);
                        checkBlob.setY(pos[1]);
                    }
                }
            }
//
//            for (int j = collideBlobs.size() - 1; j >= 0; --j) {
//                Blob blob = collideBlobs.get(j);
//
//                double rDiff = blob.getPhysicsRadius() - playerBlob.getPhysicsRadius();
//
//                if (checkCollision(playerBlob, blob) && rDiff < 0) {
//                    switch (blob.getType()) {
//                        case FOOD -> playerBlob.mass.set(playerBlob.mass.get() + blob.mass.get());
//                        case PELLET -> playerBlob.mass.set(playerBlob.mass.get() + pelletConsumeMass);
//                        case SPIKE -> {
//                            playerBlob.mass.set(playerBlob.mass.get() + virusConsumeMass);
//
//                            if (playerBlobs.size() < playerMaxSplits) {
//                                for (int k = 0; k < (playerMaxSplits - playerBlobs.size()) * 2 / 3; ++k) {
//                                    playerSplit(time, true, playerBlob);
//                                }
//                            }
//                        }
//                        case PLAYER -> {} // TODO: Networking and reworking
//                        default -> System.out.println("Unknown blob interaction type: " + blob.getType());
//                    }
//
//                    blob.removeFromPane();
//                    blob.removeFromArray();
//                }
//            }
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
            physicsUpdate.playerBlobs.values().forEach(blob -> {
                if (playerBlobs.containsKey(blob.uuid)) {
                    playerBlobs.get(blob.uuid).updatePhysicsDataTick(now);
                } else {
                    playerBlobs.put(blob.uuid, blob);
                }
            });

            physicsUpdate = null;
            lastPhysicsUpdate = now;
        } else if (lastPhysicsUpdate + 10 < now) {
            removeFromPane();
        }
    }

    public void updatePhysics(Player player) {
        physicsUpdate = player;

        physicsUpdate.playerBlobs.values().forEach(blob -> {
            if (playerBlobs.containsKey(blob.uuid)) {
                playerBlobs.get(blob.uuid).updatePhysics(blob);
            }
        });
    }

    public static Player fromJSON(JSONObject data, Game game) {
        JSONArray playerBlobsData = data.getJSONArray("player_blobs");
        HashMap<UUID, PlayerBlob> playerBlobs = new HashMap<>();

        for (int i = 0; i < playerBlobsData.length(); ++i) {
            PlayerBlob playerBlob = PlayerBlob.fromJSON(playerBlobsData.getJSONObject(i), game);
            playerBlobs.put(playerBlob.uuid, playerBlob);
        }

        return new Player(game, UUID.fromString(data.getString("uuid")), playerBlobs);
    }

}
