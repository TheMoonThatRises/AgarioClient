package ceccs.network.data;

import org.json.JSONObject;

public record IdentifyPacket(String username, double screenWidth, double screenHeight) {

    public JSONObject toJSON() {
        return new JSONObject()
                .put("username", username)
                .put("screen_width", screenWidth)
                .put("screen_height", screenHeight);
    }

}
