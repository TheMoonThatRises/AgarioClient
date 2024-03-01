package ceccs.game.objects;

import ceccs.Client;
import javafx.beans.property.ReadOnlyDoubleProperty;

public class Camera {

    private double x;
    private double y;

    private double targetX;
    private double targetY;

    private double camXV;
    private double camYV;

    private double camPosAX;
    private double camPosAY;

    private double targetCameraScale;
    private double cameraScale;

    private double vC;
    private double aC;

    public Camera() {
        this.targetCameraScale = calculateScale(10);
        this.cameraScale = targetCameraScale;

        this.vC = 0;
        this.aC = 0.0008;

        this.camXV = 0;
        this.camYV = 0;
        this.camPosAX = 0;
        this.camPosAY = 0;
    }

    public void setMass(ReadOnlyDoubleProperty mass) {
        mass.addListener(
                (observable, oldValue, newValue) ->
                        targetCameraScale = calculateScale(newValue.doubleValue())
        );

        this.targetCameraScale = calculateScale(mass.doubleValue());
    }

    public double calculateScale(double mass) {
        double dv = 50_000 / 300.0;
        double n = Math.log(dv) / Math.log(10) / 3;
        double A = 300 * Math.pow(10, 4 * n);

        double screenFactor = A / Math.pow(mass, n);

        return (Math.pow(Client.screenWidth, 2) * Math.PI) / (screenFactor * mass);
    }

    public double getCameraScale() {
        return cameraScale;
    }

    public void smoothCameraTick() {
//        if (targetX != x) {
//            camPosAX = (targetX - x) / 1E4;
//
//            camXV += camPosAX;
//
//            x = camPosAX > 0
//                ? Math.max(targetX, x + camXV)
//                : Math.min(targetX, x + camXV);
//        } else if (Math.abs(camXV) > 0) {
//            camXV = 0;
//        }
//
//        if (targetY != y) {
//            camPosAY = (targetY - y) / 1E4;
//
//            camYV += camPosAY;
//
//            y = camPosAY > 0
//                    ? Math.max(targetY, y + camYV)
//                    : Math.min(targetY, y + camYV);
//        } else if (Math.abs(camYV) > 0) {
//            camYV = 0;
//        }

        if (cameraScale != targetCameraScale) {
            aC = cameraScale > targetCameraScale ? -Math.abs(aC) : Math.abs(aC);
            vC += aC * 1 / (cameraScale / 300);

            cameraScale = aC < 0
                    ? Math.max(targetCameraScale, cameraScale + vC)
                    : Math.min(targetCameraScale, cameraScale + vC);
        } else if (Math.abs(vC) > 0) {
            vC = 0;
        }
    }

    public void setX(double targetX) {
//        this.targetX = targetX;
        this.x = targetX;
    }

    public void setY(double targetY) {
//        this.targetY = targetY;
        this.y = targetY;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

}
