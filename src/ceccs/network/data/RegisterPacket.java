package ceccs.network.data;

import ceccs.network.utils.CustomID;
import org.json.JSONObject;

public record RegisterPacket(double width, double height, double maxFramerate, CustomID playerUUID) {

    public static RegisterPacket fromJSON(JSONObject data) {
        return new RegisterPacket(
                data.getDouble("width"),
                data.getDouble("height"),
                data.getDouble("max_framerate"),
                CustomID.fromString(data.getString("player_uuid"))
        );
    }

}
