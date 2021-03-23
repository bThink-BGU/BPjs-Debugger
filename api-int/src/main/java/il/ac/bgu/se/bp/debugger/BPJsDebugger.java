package il.ac.bgu.se.bp.debugger;

import il.ac.bgu.se.bp.rest.response.GetSyncSnapshotsResponse;
import il.ac.bgu.se.bp.utils.observer.BPEvent;
import il.ac.bgu.se.bp.utils.observer.Publisher;

import java.util.Map;

public interface BPJsDebugger<T> extends Debugger<T>, Publisher<BPEvent> {

    T setup(Map<Integer, Boolean> breakpoints, boolean isSkipBreakpoints, boolean isSkipSyncPoints);

    boolean isSetup();
    boolean isStarted();

    T addExternalEvent(String externalEvent);
    T removeExternalEvent(String externalEvent);

    T setWaitForExternalEvents(boolean shouldWait);

    T startSync(Map<Integer, Boolean> breakpointsMap, boolean isSkipSyncPoints, boolean isSkipBreakpoints);

    T nextSync();
    T setIsSkipSyncPoints(boolean isSkipSyncPoints);
    GetSyncSnapshotsResponse getSyncSnapshotsHistory();
    T setSyncSnapshots(long snapShotTime);

    RunnerState getDebuggerState();
}
