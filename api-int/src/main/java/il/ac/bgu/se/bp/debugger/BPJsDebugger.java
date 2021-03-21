package il.ac.bgu.se.bp.debugger;

import il.ac.bgu.se.bp.rest.response.GetSyncSnapshotsResponse;

import java.util.Map;

public interface BPJsDebugger<T> extends Debugger<T> {

    T setup(Map<Integer, Boolean> breakpoints, boolean isSkipBreakpoints, boolean isSkipSyncPoints);

    boolean isSetup();
    boolean isStarted();

    T addExternalEvent(String externalEvent);
    T removeExternalEvent(String externalEvent);

    T setWaitForExternalEvents(boolean shouldWait);

    T startSync(boolean isSkipBreakpoints, boolean isSkipSyncPoints);

    T nextSync();
    T setIsSkipSyncPoints(boolean isSkipSyncPoints);
    GetSyncSnapshotsResponse getSyncSnapshotsHistory();
    T setSyncSnapshots(long snapShotTime);

    RunnerState getDebuggerState();
}
