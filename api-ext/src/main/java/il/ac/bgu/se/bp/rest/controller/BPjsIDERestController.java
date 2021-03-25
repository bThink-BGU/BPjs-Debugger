package il.ac.bgu.se.bp.rest.controller;

import il.ac.bgu.se.bp.rest.request.*;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;

import java.security.Principal;

public interface BPjsIDERestController {

    void subscribeUser(String sessionId, Principal principal);

    BooleanResponse run(String userId, RunRequest code);
    BooleanResponse debug(String userId, DebugRequest code);

    BooleanResponse setBreakpoint(String userId, SetBreakpointRequest setBreakpointRequest);
    BooleanResponse toggleMuteBreakpoints(String userId, ToggleBreakpointsRequest toggleBreakpointsRequest);
    BooleanResponse toggleMuteSyncPoints(String userId, ToggleSyncStatesRequest toggleMuteSyncPoints);

    BooleanResponse stop(String userId);
    BooleanResponse stepOut(String userId);
    BooleanResponse stepInto(String userId);
    BooleanResponse stepOver(String userId);
    BooleanResponse continueRun(String userId);

    BooleanResponse nextSync(String userId);

    BooleanResponse externalEvent(String userId, ExternalEventRequest externalEventRequest);
    BooleanResponse setSyncSnapshot(String userId, SetSyncSnapshotRequest setSyncSnapshotRequest);

}
