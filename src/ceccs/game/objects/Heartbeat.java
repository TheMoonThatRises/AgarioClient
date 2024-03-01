package ceccs.game.objects;

import javafx.animation.AnimationTimer;

import java.util.ArrayList;

public class Heartbeat extends AnimationTimer {

    public interface Routine {
        void routine(long now);
    }

    ArrayList<Routine> routines;

    public Heartbeat() {
        this.routines = new ArrayList<>();
    }

    public void addRoutine(Routine routine) {
        this.routines.add(routine);
    }

    @Override
    public void handle(long now) {
        routines.forEach(routine -> routine.routine(now));
    }

}
