package ceccs.game.objects.ui;

import ceccs.Client;
import ceccs.game.objects.BLOB_TYPES;
import ceccs.game.panes.game.Game;
import ceccs.network.utils.CustomID;
import ceccs.utils.InternalException;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static ceccs.game.configs.PlayerConfigs.*;
import static ceccs.game.configs.VirusConfigs.virusMass;
import static ceccs.game.utils.Utilities.*;
import static ceccs.utils.InternalException.checkSafeDivision;

public class Player {

    final public CustomID uuid;
    final protected ObservableMap<CustomID, PlayerBlob> playerBlobs;
    final protected HashMap<KeyCode, Boolean> keyEvents;
    final protected Game game;
    final protected String username;
    protected DoubleProperty totalMass;
    protected NumberBinding massBinding;
    protected MouseEvent mouseEvent;
    protected Player physicsUpdate;

    protected Player(Game game, CustomID uuid, String username, JSONArray playerBlobs) {
        this.game = game;

        this.username = username;

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

        this.playerBlobs.addListener((MapChangeListener<CustomID, PlayerBlob>) change -> {
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
            PlayerBlob playerBlob = PlayerBlob.fromJSON(playerBlobs.getJSONObject(i), game, this);
            this.playerBlobs.put(playerBlob.uuid, playerBlob);
        }

        this.mouseEvent = null;
        this.keyEvents = new HashMap<>();
    }

    public static Player fromJSON(JSONObject data, Game game) {
        JSONArray playerBlobsData = data.getJSONArray("player_blobs");

        game.camera.updateCamera(data.getJSONObject("camera"));

        return new Player(
                game,
                CustomID.fromString(data.getString("uuid")),
                data.getString("username"),
                playerBlobsData
        );
    }

    public void removeFromPane() {
        playerBlobs.values().forEach(PlayerBlob::removeFromPane);
    }

    public void toFront(boolean spike) {
        playerBlobs.values()
                .stream()
                .filter(blob -> spike
                        ? blob.mass.greaterThanOrEqualTo(virusMass).get()
                        : blob.mass.lessThan(virusMass).get())
                .forEach(PlayerBlob::allToFront);
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
        ArrayList<CustomID> uuidList = new ArrayList<>(playerBlobs.keySet());

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
                            checkBlob.removeFromMap();
                        }
                    } else if (checkTouch(playerBlob, checkBlob) || checkCollision(playerBlob, checkBlob)) {
                        double collisionTheta = blobTheta(playerBlob, checkBlob);
                        double collisionDelta = overlapDelta(playerBlob, checkBlob);

                        checkBlob.axForces.add(collisionDelta * Math.cos(collisionTheta));
                        checkBlob.ayForces.add(collisionDelta * Math.sin(collisionTheta));
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

    public void updatePhysicsDataTick() {
        if (physicsUpdate != null) {
            ArrayList<CustomID> uuidList = new ArrayList<>(playerBlobs.keySet());

            for (int i = uuidList.size() - 1; i >= 0; --i) {
                playerBlobs.get(uuidList.get(i)).updatePhysicsDataTick();
            }

            physicsUpdate.playerBlobs.values().forEach(blob -> {
                if (!playerBlobs.containsKey(blob.uuid)) {
                    playerBlobs.put(blob.uuid, new PlayerBlob(blob, this));
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

    public String getUsername() {
        return username.isBlank() ? "Unnamed blob" : username;
    }

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

        final public CustomID parentUUID;
        final protected ConcurrentLinkedQueue<Double> axForces;
        final protected ConcurrentLinkedQueue<Double> ayForces;
        final protected Cooldowns cooldowns;
        final protected Player parentPlayer;
        final protected Text blobName;
        final protected Text massText;
        final protected VBox blobLabel;
        final protected StackPane parentPane;
        protected double maxVx;
        protected double maxVy;
        protected double relX;
        protected double relY;
        protected boolean hasSplitSpeedBoost;
        protected double splitBoostVelocity;
        protected PlayerBlob physicsUpdate;

        public PlayerBlob(double x, double y, double vx, double vy, double ax, double ay, ConcurrentLinkedQueue<Double> axForces, ConcurrentLinkedQueue<Double> ayForces, double mass, boolean hasSplitSpeedBoost, Paint fill, Game game, CustomID parentUUID, CustomID uuid, Player parentPlayer) {
            super(x, y, vx, vy, ax, ay, mass, fill, game, uuid, null);

            this.maxVx = 0;
            this.maxVy = 0;

            this.axForces = axForces;
            this.ayForces = ayForces;

            this.relX = 0;
            this.relY = 0;

            this.hasSplitSpeedBoost = hasSplitSpeedBoost;
            this.splitBoostVelocity = playerSplitVelocity;

            this.cooldowns = new Cooldowns();

            this.parentUUID = parentUUID;

            this.parentPlayer = parentPlayer;

            this.blobName = new Text(parentPlayer.getUsername());
            this.blobName.setFont(veraMono);
            this.blobName.setBoundsType(TextBoundsType.VISUAL);

            this.massText = new Text();
            this.massText.setFont(veraMono);
            this.massText.setBoundsType(TextBoundsType.VISUAL);
            this.massText.textProperty().bind(massProperty().asString("%.2f"));

            this.blobLabel = new VBox(10, blobName, massText);
            this.blobLabel.setAlignment(Pos.CENTER);

            this.parentPane = new StackPane(this, this.blobLabel);

            this.blobLabel.toFront();

            super.setVisible(true);
        }

        public PlayerBlob(PlayerBlob playerBlob, Player parentPlayer) {
            this(
                    playerBlob.x, playerBlob.y, playerBlob.vx, playerBlob.vy, playerBlob.ax, playerBlob.ay,
                    playerBlob.axForces, playerBlob.ayForces,
                    playerBlob.mass.get(), playerBlob.hasSplitSpeedBoost,
                    playerBlob.getFill(), playerBlob.game, playerBlob.parentUUID, playerBlob.uuid,
                    parentPlayer
            );
        }

        public static PlayerBlob fromJSON(JSONObject data, Game game, Player parentPlayer) {
            ConcurrentLinkedQueue<Double> axForces = new ConcurrentLinkedQueue<>(
                    data.getJSONArray("ax_forces")
                            .toList()
                            .stream()
                            .map(value -> Double.valueOf(value.toString()))
                            .toList()
            );
            ConcurrentLinkedQueue<Double> ayForces = new ConcurrentLinkedQueue<>(
                    data.getJSONArray("ay_forces")
                            .toList()
                            .stream()
                            .map(value -> Double.valueOf(value.toString()))
                            .toList()
            );

            return new PlayerBlob(
                    data.getDouble("x"), data.getDouble("y"), data.getDouble("vx"), data.getDouble("vy"),
                    data.getDouble("ax"), data.getDouble("ay"),
                    axForces, ayForces,
                    data.getDouble("mass"),
                    data.getBoolean("has_split_speed_boost"), Paint.valueOf(data.getString("fill")),
                    game, CustomID.fromString(data.getString("parent_uuid")), CustomID.fromString(data.getString("uuid")),
                    parentPlayer
            );
        }

        @Override
        public BLOB_TYPES getType() {
            return BLOB_TYPES.PLAYER;
        }

        @Override
        public void addToPane() {
            game.getChildren().add(parentPane);
        }

        @Override
        public void removeFromPane() {
            game.getChildren().remove(parentPane);
        }

        @Override
        public void removeFromMap() {
            parentPlayer.playerBlobs.remove(uuid);
        }

        public void allToFront() {
            parentPane.toFront();
        }

        public void positionTick(MouseEvent mouseEvent) {
            if (mouseEvent != null && !mouseEvent.isConsumed()) {
                relX = mouseEvent.getX() - getRelativeX();
                relY = mouseEvent.getY() - getRelativeY();

                maxVx = playerVelocities[closestNumber(playerVelocities, relX / 1000)];
                maxVy = playerVelocities[closestNumber(playerVelocities, relY / 1000)];
            }

            double velScale;

            try {
                velScale = calcVelocityModifier(mass.get());
            } catch (InternalException exception) {
                exception.printStackTrace();

                System.err.println("mass is zero in position tick");

                return;
            }

            double axTrue = maxVx < 0
                    ? -Math.abs(ax)
                    : Math.abs(ax);
            double ayTrue = maxVy < 0
                    ? -Math.abs(ay)
                    : Math.abs(ay);

            vx = (
                    Math.abs(vx) < Math.abs(maxVx)
                            ? vx + axTrue
                            : maxVx
            );
            vy = (
                    Math.abs(vy) < Math.abs(maxVy)
                            ? vy + ayTrue
                            : maxVy
            );

            vx += axForces.stream().reduce(0.0, Double::sum);
            vy += ayForces.stream().reduce(0.0, Double::sum);

            if (hasSplitSpeedBoost) {
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

            axForces.clear();
            ayForces.clear();
        }

        @Override
        public void animationTick() {
            if (parentPane.getParent() == null) {
                addToPane();
            }

            double relX = getRelativeX();
            double relY = getRelativeY();
            double relRadius = getPhysicsRadius() * game.camera.getCameraScale();

            if (
                    relX + relRadius < -10 ||
                            relX - relRadius > Client.screenWidth + 10 ||
                            relY + relRadius < -10 ||
                            relY - relRadius > Client.screenHeight + 10 ||
                            relRadius < 0.5
            ) {
                if (parentPane.isVisible()) {
                    parentPane.setVisible(false);
                }

                return;
            } else if (!parentPane.isVisible()) {
                parentPane.setVisible(true);
            }

            parentPane.setLayoutX(relX - parentPane.getWidth() / 2);
            parentPane.setLayoutY(relY - parentPane.getHeight() / 2);

            setRadius(relRadius);
        }

        @Override
        public void updatePhysicsDataTick() {
            if (this.physicsUpdate != null) {
                maxVx = physicsUpdate.maxVx;
                maxVy = physicsUpdate.maxVy;

                axForces.clear();
                ayForces.clear();

                axForces.addAll(physicsUpdate.axForces);
                ayForces.addAll(physicsUpdate.ayForces);

                hasSplitSpeedBoost = physicsUpdate.hasSplitSpeedBoost;
                splitBoostVelocity = physicsUpdate.splitBoostVelocity;

                cooldowns.updateCooldowns(physicsUpdate.cooldowns);

                super.updatePhysicsDataTick();

                this.physicsUpdate = null;
            } else {
                removeFromPane();
                removeFromMap();
            }
        }

        public void updatePhysics(PlayerBlob blob) {
            this.physicsUpdate = blob;

            super.updatePhysics(blob);
        }
    }

}
