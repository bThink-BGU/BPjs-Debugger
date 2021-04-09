package il.ac.bgu.se.bp.debugger;

import il.ac.bgu.se.bp.rest.response.GetSyncSnapshotsResponse;
import il.ac.bgu.se.bp.socket.state.EventInfo;
import il.ac.bgu.se.bp.utils.observer.BPEvent;
import il.ac.bgu.se.bp.utils.observer.Publisher;

import java.util.Map;
import java.util.SortedMap;

public interface BPJsDebugger<T> extends Debugger<T>, Publisher<BPEvent> {

    T setup(Map<Integer, Boolean> breakpoints, boolean isSkipBreakpoints, boolean isSkipSyncPoints);

    boolean isSetup();
    boolean isStarted();

    T addExternalEvent(String externalEvent);
    T removeExternalEvent(String externalEvent);

    T setWaitForExternalEvents(boolean shouldWait);

    T startSync(Map<Integer, Boolean> breakpointsMap, boolean isSkipSyncPoints, boolean isSkipBreakpoints);

    T nextSync();
    T toggleMuteSyncPoints(boolean toggleMuteSyncPoints);
    GetSyncSnapshotsResponse getSyncSnapshotsHistory();
    T setSyncSnapshot(long snapShotTime);

    RunnerState getDebuggerState();

    SortedMap<Long, EventInfo> getEventsHistory(int from, int to);
}
