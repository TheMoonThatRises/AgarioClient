package ceccs.network.data;

import org.json.JSONObject;

import java.util.UUID;

public record RegisterPacket(double width, double height, double maxFramerate, UUID playerUUID) {

    public static RegisterPacket fromJSON(JSONObject data) {
        return new RegisterPacket(
                data.getDouble("width"),
                data.getDouble("height"),
                data.getDouble("max_framerate"),
                UUID.fromString(data.getString("player_uuid"))
        );
    }

}
