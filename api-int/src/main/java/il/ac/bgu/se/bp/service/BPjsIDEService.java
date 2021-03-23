package il.ac.bgu.se.bp.service;

import il.ac.bgu.se.bp.rest.request.*;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;

public interface BPjsIDEService {

    BooleanResponse subscribeUser(String sessionId, String userId);

    BooleanResponse run(RunRequest runRequest, String userId);
    BooleanResponse debug(DebugRequest debugRequest, String userId);

    BooleanResponse setBreakpoint(String userId, SetBreakpointRequest setBreakpointRequest);
    BooleanResponse toggleMuteBreakpoints(String userId, ToggleBreakpointsRequest toggleBreakPointStatus);
    BooleanResponse toggleMuteSyncPoints(String userId, ToggleSyncStatesRequest toggleMuteSyncPoints);

    BooleanResponse stop(String userId);
    BooleanResponse stepOut(String userId);
    BooleanResponse stepInto(String userId);
    BooleanResponse stepOver(String userId);
    BooleanResponse continueRun(String userId);

    BooleanResponse nextSync(String userId);

    BooleanResponse externalEvent(String userId, ExternalEventRequest externalEventRequest);

//    BooleanResponse setSyncSnapshots(long snapShotTime);
//    BooleanResponse setWaitForExternalEvents(boolean shouldWait);
//    GetSyncSnapshotsResponse getSyncSnapshotsHistory();
}
