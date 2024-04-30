package ceccs.game.objects.ui;

import ceccs.Client;
import ceccs.game.objects.BLOB_TYPES;
import ceccs.game.panes.game.Game;
import ceccs.game.scenes.GameScene;
import ceccs.network.utils.CustomID;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.CacheHint;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import org.json.JSONObject;

import java.util.AbstractMap;

public class Blob extends Circle {

    final public CustomID uuid;
    final protected double initialVx;
    final protected double initialVy;
    final protected DoubleProperty mass;
    final protected Game game;
    final protected AbstractMap<CustomID, ? extends Blob> parentMap;
    protected double x;
    protected double y;
    protected double vx;
    protected double vy;
    protected double ax;
    protected double ay;
    protected Blob physicsUpdate;

    public Blob(double x, double y, double vx, double vy, double ax, double ay, double mass, Paint fill, Game game, CustomID uuid, AbstractMap<CustomID, ? extends Blob> parentMap) {
        super(x, y, Math.sqrt(mass / Math.PI), fill);

        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.ax = ax;
        this.ay = ay;
        this.mass = new DoublePropertyBase(mass) {
            @Override
            public Object getBean() {
                return Blob.this;
            }

            @Override
            public String getName() {
                return "mass";
            }
        };

        this.initialVx = vx;
        this.initialVy = vy;

        this.game = game;

        this.parentMap = parentMap;

        this.uuid = uuid;

        this.physicsUpdate = null;

        setVisible(false);

        setCache(true);
        setCacheHint(CacheHint.SPEED);
    }

    public static Blob fromJSON(JSONObject data, Game game, AbstractMap<CustomID, ? extends Blob> parent) {
        return new Blob(
                data.getDouble("x"), data.getDouble("y"), data.getDouble("vx"), data.getDouble("vy"),
                data.getDouble("ax"), data.getDouble("ay"), data.getDouble("mass"),
                Paint.valueOf(data.getString("fill")), game,
                CustomID.fromString(data.getString("uuid")),
                parent
        );
    }

    public BLOB_TYPES getType() {
        return BLOB_TYPES.GENERIC;
    }

    public void addToPane() {
        game.getChildren().add(this);
    }

    public void removeFromPane() {
        game.getChildren().remove(this);
    }

    public void removeFromMap() {
        if (parentMap != null) {
            parentMap.remove(uuid);
        }
    }

    public void positionTick() {
        vx += ax;
        vy += ay;

        x += vx;
        y += vy;
    }

    public void collisionTick() {
        double radius = getPhysicsRadius();

        if (x - radius < 0) {
            x = radius;
            vx = 0;
        } else if (x + radius > GameScene.registerPacket.width()) {
            x = GameScene.registerPacket.width() - radius;
            vx = 0;
        }

        if (y - radius < 0) {
            y = radius;
            vy = 0;
        } else if (y + radius > GameScene.registerPacket.height()) {
            y = GameScene.registerPacket.height() - radius;
            vy = 0;
        }
    }

    public void animationTick() {
        if (getParent() == null) {
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
            if (isVisible()) {
                setVisible(false);
            }

            return;
        } else if (!isVisible()) {
            setVisible(true);
        }

        setCenterX(relX);
        setCenterY(relY);

        setRadius(relRadius);
    }

    public double getRelativeX() {
        return (x - game.camera.getX()) * game.camera.getCameraScale();
    }

    public double getRelativeY() {
        return (y - game.camera.getY()) * game.camera.getCameraScale();
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void updatePhysicsDataTick() {
        if (physicsUpdate != null) {
            this.x = physicsUpdate.x;
            this.y = physicsUpdate.y;
            this.vx = physicsUpdate.vx;
            this.vy = physicsUpdate.vy;
            this.ax = physicsUpdate.ax;
            this.ay = physicsUpdate.ay;
            this.mass.set(physicsUpdate.mass.doubleValue());

            physicsUpdate = null;
        } else {
            removeFromPane();
            removeFromMap();
        }
    }

    public void updatePhysics(Blob blob) {
        this.physicsUpdate = blob;
    }

    public double getPhysicsRadius() {
        return Math.sqrt(mass.get() / Math.PI);
    }

    public ReadOnlyDoubleProperty massProperty() {
        return mass;
    }

}
