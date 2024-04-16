package ceccs.network.data;

import org.json.JSONObject;

public record KeyPacket(int keycode, boolean pressed) {

    public JSONObject toJSON() {
        return new JSONObject()
                .put("keycode", keycode)
                .put("pressed", pressed);
    }

}
