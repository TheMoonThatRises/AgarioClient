package ceccs.network.data;

import org.json.JSONObject;

public record IdentifyPacket(String username, double screenWidth, double screenHeight) {

    public JSONObject toJSON() {
        return new JSONObject(String.format(
                "{\"username\":\"%s\",\"screen_width\":%f,\"screen_height\":%f}",
                username, screenWidth, screenHeight
        ));
    }

}
