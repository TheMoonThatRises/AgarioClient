package ceccs.network.data;

import org.json.JSONObject;

public class KeyPacket {

    final public int keycode;
    final public boolean pressed;

    public KeyPacket(int keycode, boolean pressed) {
        this.keycode = keycode;
        this.pressed = pressed;
    }

    public JSONObject toJSON() {
        return new JSONObject(String.format(
            "{\"keycode\":%d,\"pressed\":%s}",
            keycode, pressed ? "true" : "false"
        ));
    }

}
