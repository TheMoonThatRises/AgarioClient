package ceccs.network.data;

import org.json.JSONObject;

public record KeyPacket(int keycode, boolean pressed) {

    public JSONObject toJSON() {
        return new JSONObject(String.format(
                "{\"keycode\":%d,\"pressed\":%s}",
                keycode, pressed ? "true" : "false"
        ));
    }

}
