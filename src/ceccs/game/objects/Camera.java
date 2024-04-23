package ceccs.game.objects;

import ceccs.Client;
import ceccs.utils.InternalException;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.input.ScrollEvent;
import org.json.JSONObject;

public class Camera {

    private double x;
    private double y;

    private double targetCameraScale;
    private double cameraScale;

    private double scrollScale;

    private double cacheMass;

    private double vC;
    private double aC;

    public Camera() {
        try {
            this.targetCameraScale = calculateScale(10);
        } catch (InternalException exception) {
            exception.printStackTrace();

            System.err.println("this should never be reached");
        }

        this.cameraScale = targetCameraScale;

        this.scrollScale = 1;
        this.cacheMass = 0;

        this.vC = 0;
        this.aC = 0.0008;
    }

    public void setMass(ReadOnlyDoubleProperty mass) throws InternalException {
        mass.addListener((observable, oldValue, newValue) -> {
            cacheMass = newValue.doubleValue();
            try {
                targetCameraScale = calculateScale(newValue.doubleValue());
            } catch (InternalException exception) {
                exception.printStackTrace();

                System.err.println("player mass is zero?");
            }
        });

        this.targetCameraScale = calculateScale(mass.doubleValue());
        this.cacheMass = mass.doubleValue();
    }

    public void updateScrollWheel(ScrollEvent event) throws InternalException {
        this.scrollScale = Math.max(Math.min(scrollScale - event.getDeltaX() / 50, 4), 1);
        this.targetCameraScale = calculateScale(cacheMass);
    }

    public double calculateScale(double mass) throws InternalException {
        double dv = 50_000 / 300.0;
        double n = Math.log(dv) / Math.log(10) / 3;
        double A = 300 * Math.pow(10, 4 * n);

        double screenFactor = A / Math.pow(mass, n);

        if (mass == 0) {
            throw new InternalException("unsafe zero: mass = " + mass);
        }

        return (Math.pow(Client.screenWidth, 2) * Math.PI) / (screenFactor * mass) * scrollScale;
    }

    public double getCameraScale() {
        return cameraScale;
    }

    public void smoothCameraTick() {
        if (cameraScale != targetCameraScale) {
            aC = (cameraScale > targetCameraScale ? -Math.abs(aC) : Math.abs(aC)) * scrollScale;
            vC += aC * 1 / (cameraScale / 300);

            cameraScale = aC < 0
                    ? Math.max(targetCameraScale, cameraScale + vC)
                    : Math.min(targetCameraScale, cameraScale + vC);
        } else if (Math.abs(vC) > 0) {
            vC = 0;
        }
    }

    public double getX() {
        return x;
    }

    public void setX(double targetX) {
        this.x = targetX;
    }

    public double getY() {
        return y;
    }

    public void setY(double targetY) {
        this.y = targetY;
    }

    public void updateCamera(JSONObject camera) {
        this.x = camera.getDouble("x");
        this.y = camera.getDouble("y");
        this.cameraScale = camera.getDouble("scale");
    }

}
