package il.ac.bgu.se.bp.service;

import il.ac.bgu.se.bp.rest.request.DebugRequest;
import il.ac.bgu.se.bp.rest.request.RunRequest;
import il.ac.bgu.se.bp.rest.request.SetBreakpointRequest;
import il.ac.bgu.se.bp.rest.request.ToggleBreakpointsRequest;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;

public interface BPjsIDEService {

    BooleanResponse subscribeUser(String sessionId, String userId);

    BooleanResponse run(RunRequest runRequest, String userId);
    BooleanResponse debug(DebugRequest debugRequest, String userId);

    BooleanResponse setBreakpoint(String userId, SetBreakpointRequest setBreakpointRequest);
    BooleanResponse toggleMuteBreakpoints(String userId, ToggleBreakpointsRequest toggleBreakPointStatus);

    BooleanResponse stop(String userId);
    BooleanResponse stepOut(String userId);
    BooleanResponse stepInto(String userId);
    BooleanResponse stepOver(String userId);
    BooleanResponse continueRun(String userId);

    BooleanResponse nextSync(String userId);


//    BooleanResponse nextSync();
//    BooleanResponse setSyncSnapshots(long snapShotTime);
//
//    BooleanResponse addExternalEvent(String externalEvent);
//    BooleanResponse removeExternalEvent(String externalEvent);
//    BooleanResponse setWaitForExternalEvents(boolean shouldWait);
//    BooleanResponse setIsSkipSyncPoints(boolean isSkipSyncPoints);
//    GetSyncSnapshotsResponse getSyncSnapshotsHistory();
}
