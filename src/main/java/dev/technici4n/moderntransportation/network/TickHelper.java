package dev.technici4n.moderntransportation.network;

import java.util.ArrayList;
import java.util.List;

public class TickHelper {
    private static long tickCounter = 0;
    private static List<Runnable> delayedActions = new ArrayList<>();
    private static List<Runnable> delayedActions2 = new ArrayList<>();

    public static long getTickCounter() {
        return tickCounter;
    }

    public static synchronized void onEndTick() {
        tickCounter++;

        List<Runnable> actionsToProcess = delayedActions;
        delayedActions = delayedActions2;
        delayedActions2 = actionsToProcess;

        for (Runnable runnable : actionsToProcess) {
            runnable.run();
        }

        actionsToProcess.clear();
    }

    // TODO: thread safety checks
    public static synchronized void runLater(Runnable runnable) {
        delayedActions.add(runnable);
    }
}
