package ceccs.game.objects.ui;

import ceccs.game.objects.BLOB_TYPES;
import ceccs.game.panes.Game;
import javafx.scene.paint.Paint;

import java.util.UUID;

public class Pellet extends Blob {

    public Pellet(double x, double y, double vx, double vy, double ax, double ay, double mass, Paint fill, Game game, UUID uuid) {
        super(x, y, vx, vy, ax, ay, mass, fill, game, uuid, game.pellets);
    }

    @Override
    public BLOB_TYPES getType() {
        return BLOB_TYPES.PELLET;
    }

    public static Pellet fromBlob(Blob blob) {
        return new Pellet(
            blob.x, blob.y, blob.vx, blob.vy, blob.ax, blob.ay, blob.mass.get(), blob.getFill(), blob.game, blob.uuid
        );
    }

}
