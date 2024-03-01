package ceccs.network.data;

import org.json.JSONObject;

import java.util.UUID;

public class RegisterPacket {

    final public double width;
    final public double height;

    final public UUID playerUUID;

    public RegisterPacket(double width, double height, UUID playerUUID) {
        this.width = width;
        this.height = height;

        this.playerUUID = playerUUID;
    }

    public static RegisterPacket fromJSON(JSONObject data) {
        return new RegisterPacket(
            data.getDouble("width"),
            data.getDouble("height"),
            UUID.fromString(data.getString("player_uuid"))
        );
    }

}
