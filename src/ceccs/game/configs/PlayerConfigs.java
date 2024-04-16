package ceccs.game.configs;

import ceccs.utils.InternalException;

public class PlayerConfigs {

    final static public double playerSplitVelocity = 4;
    final static public double playerSplitDecay = 0.1;

    final static public double[] playerVelocities = new double[]{
            -0.01, -0.05, -0.1, -0.2, -0.4, -0.5,
            0,
            0.01, 0.05, 0.1, 0.2, 0.4, 0.5
    };

    public static double calcVelocityModifier(double mass) throws InternalException {
        double dv = 2 / 0.1;
        double n = Math.log(dv) / Math.log(10) / 3;
        double A = 0.1 * Math.pow(10, 4 * n);

        if (mass == 0) {
            throw new InternalException("unsafe zero: mass = " + mass);
        }

        return A / Math.pow(mass, n);
    }

}
