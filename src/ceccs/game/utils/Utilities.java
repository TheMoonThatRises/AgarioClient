package ceccs.game.utils;

import ceccs.Client;
import ceccs.game.objects.ui.Blob;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.security.SecureRandom;

public class Utilities {

    final public static SecureRandom random = new SecureRandom();
    final public static Font veraMono;
    final private static boolean randomise = true;
    final private static long seed = 3249871132234509L;

    static {
        if (!randomise) {
            random.setSeed(seed);
        }

        Font customFont;

        try {
            customFont = Font.loadFont(
                    Client.class
                            .getResource("/bitstream_vera_sans_mono/VeraMono.ttf")
                            .openStream(),
                    12
            );
        } catch (Exception e) {
            e.printStackTrace();

            System.out.println("unable to load custom font");

            customFont = Font.font("Courier New", 12);
        }

        veraMono = customFont;
    }

    public static double[] checkValues(Blob blob1, Blob blob2) {
        double xDist = blob2.getX() - blob1.getX();
        double yDist = blob2.getY() - blob1.getY();
        double dst = xDist * xDist + yDist * yDist;
        double rDiff = blob2.getPhysicsRadius() - blob1.getPhysicsRadius();
        double rSum = blob2.getPhysicsRadius() + blob1.getPhysicsRadius();

        return new double[]{dst, rDiff, rSum};
    }

    public static boolean checkCollision(Blob blob1, Blob blob2) {
        double[] values = checkValues(blob1, blob2);

        return values[0] < values[1] * values[1];
    }

    public static boolean checkTouch(Blob blob1, Blob blob2) {
        double[] values = checkValues(blob1, blob2);

        return values[0] <= values[2] * values[2];
    }

    public static double blobTheta(Blob blob1, Blob blob2) {
        return Math.atan2(blob2.getY() - blob1.getY(), blob2.getX() - blob1.getX());
    }

    public static double overlapDelta(Blob blob1, Blob blob2) {
        double[] values = checkValues(blob1, blob2);

        return values[2] - Math.sqrt(values[0]);
    }

    public static double[] repositionBlob(Blob blob, double r2, double delta) {
        double collisionRadius = blob.getPhysicsRadius() + r2;

        double xPos = blob.getX() + collisionRadius * Math.cos(delta);
        double yPos = blob.getY() + collisionRadius * Math.sin(delta);

        return new double[]{xPos, yPos};
    }

    public static double[] repositionBlob(Blob blob1, Blob blob2) {
        double theta = blobTheta(blob1, blob2);

        return repositionBlob(blob1, blob2.getPhysicsRadius(), theta);
    }

    // https://stackoverflow.com/a/13318769
    public static int closestNumber(double[] arr, double value) {
        double distance = Math.abs(arr[0] - value);
        int idx = 0;
        for (int c = 1; c < arr.length; c++) {
            double cdistance = Math.abs(arr[c] - value);
            if (cdistance < distance) {
                idx = c;
                distance = cdistance;
            }
        }
        return idx;
    }

    public static Color opacityColor(Color color, double opacity) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
    }

    private static String padHex(String hex, int endCount) {
        if (hex.length() < endCount) {
            hex = "0".repeat(endCount - hex.length()) + hex;
        }

        return hex;
    }

}
