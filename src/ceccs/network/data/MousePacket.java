package ceccs.network.data;

import org.json.JSONObject;

public record MousePacket(double x, double y) {

    public JSONObject toJSON() {
        return new JSONObject()
                .put("x", x)
                .put("y", y);
    }

}
