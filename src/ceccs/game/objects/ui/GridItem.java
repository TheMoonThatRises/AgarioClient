package ceccs.game.objects.ui;

import ceccs.Client;
import ceccs.game.panes.Game;
import javafx.scene.CacheHint;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class GridItem extends Line {

    protected double x1;
    protected double y1;
    protected double x2;
    protected double y2;

    protected double strokeWidth;

    protected Game game;

    public GridItem(double x1, double y1, double x2, double y2, Game game) {
        super(x1, y1, x2, y2);

        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;

        this.strokeWidth = 0.1;

        this.game = game;

        setStroke(Color.DARKGREY);

        setVisible(false);

        setCache(true);
        setCacheHint(CacheHint.SPEED);
    }

    public void animationTick() {
        double relX1 = (x1 - game.camera.getX()) * game.camera.getCameraScale();
        double relY1 = (y1 - game.camera.getY()) * game.camera.getCameraScale();
        double relX2 = (x2 - game.camera.getX()) * game.camera.getCameraScale();
        double relY2 = (y2 - game.camera.getY()) * game.camera.getCameraScale();
        double relStrokeWidth = strokeWidth * game.camera.getCameraScale();

        setStartX(relX1);
        setStartY(relY1);
        setEndX(relX2);
        setEndY(relY2);

        setStrokeWidth(relStrokeWidth);

        if (
            (
                relX1 == relX2 &&
                (relX1 - relStrokeWidth < -10 || relX1 + relStrokeWidth > Client.screenWidth + 10)
            ) ||
            (
                relY1 == relY2 &&
                (relY1 - relStrokeWidth < -10 || relY1 + relStrokeWidth > Client.screenHeight + 10)
            )
        ) {
            if (isVisible()) {
                setVisible(false);
                setCacheHint(CacheHint.SPEED);
            }
        } else if (!isVisible()) {
            setVisible(true);
            setCacheHint(CacheHint.QUALITY);
        }
    }

}
