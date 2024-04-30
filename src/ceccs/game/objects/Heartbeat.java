package ceccs.game.objects;

import ceccs.game.scenes.GameScene;
import javafx.animation.AnimationTimer;

import java.util.HashMap;

public class Heartbeat extends AnimationTimer {

    private final long[] frameTimes = new long[100];
    final private HashMap<String, Routine> routines;
    private int frameTimeIndex = 0;
    private boolean arrayFilled = false;
    private double framerate;
    private long prevTime;

    public Heartbeat() {
        this.routines = new HashMap<>();

        this.framerate = 0;
        this.prevTime = 0;
    }

    public void addRoutine(String key, Routine routine) {
        this.routines.put(key, routine);
    }

    public void removeRoutine(String key) {
        this.routines.remove(key);
    }

    @Override
    public void handle(long now) {
        if (GameScene.registerPacket != null) {
            if (now - prevTime < 1_000_000_000 / GameScene.registerPacket.maxFramerate()) {
                return;
            }

            prevTime = now;

            long oldFrameTime = frameTimes[frameTimeIndex];
            frameTimes[frameTimeIndex] = now;
            frameTimeIndex = (frameTimeIndex + 1) % frameTimes.length;
            if (frameTimeIndex == 0) {
                arrayFilled = true;
            }

            if (arrayFilled) {
                long elapsedNanos = now - oldFrameTime;
                long elapsedNanosPerFrame = elapsedNanos / frameTimes.length;
                framerate = 1_000_000_000.0 / elapsedNanosPerFrame;
            }
        }

        routines.values().forEach(routine -> routine.routine(now));
    }

    public double getFramerate() {
        return framerate;
    }

    public interface Routine {
        void routine(long now);
    }

}
