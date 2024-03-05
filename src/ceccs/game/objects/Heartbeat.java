package ceccs.game.objects;

import ceccs.Client;
import javafx.animation.AnimationTimer;

import java.util.ArrayList;

public class Heartbeat extends AnimationTimer {

    private final long[] frameTimes = new long[100];
    private int frameTimeIndex = 0;
    private boolean arrayFilled = false;

    private long prevTime;

    private double framerate;

    public interface Routine {
        void routine(long now);
    }

    final private ArrayList<Routine> routines;

    public Heartbeat() {
        this.routines = new ArrayList<>();

        this.framerate = 0;
        this.prevTime = 0;
    }

    public void addRoutine(Routine routine) {
        this.routines.add(routine);
    }

    @Override
    public void handle(long now) {
        if (now - prevTime < 1_000_000_000 / Client.registerPacket.maxFramerate()) {
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

        routines.forEach(routine -> routine.routine(now));
    }

    public double getFramerate() {
        return framerate;
    }

}
