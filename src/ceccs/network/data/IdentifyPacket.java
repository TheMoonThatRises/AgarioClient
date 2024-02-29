package ceccs.network.data;

import org.json.JSONObject;

public record IdentifyPacket(double screenWidth, double screenHeight) {

    public JSONObject toJSON() {
        return new JSONObject(String.format(
                "{\"screen_width\":%f,\"screen_height\":%f}",
                screenWidth, screenHeight
        ));
    }

}
