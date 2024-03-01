package ceccs.network.data;

import org.json.JSONObject;

public class IdentifyPacket {

    final public double screenWidth;
    final public double screenHeight;

    public IdentifyPacket(double screenWidth, double screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public JSONObject toJSON() {
        return new JSONObject(String.format(
            "{\"screen_width\":%f,\"screen_height\":%f}",
            screenWidth, screenHeight
        ));
    }

}
