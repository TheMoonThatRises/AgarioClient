package ceccs.network.data;

import org.json.JSONObject;

public record MousePacket(double x, double y) {

    public JSONObject toJSON() {
        return new JSONObject(String.format(
                "{\"x\":%f,\"y\":%f}",
                x, y
        ));
    }

}
