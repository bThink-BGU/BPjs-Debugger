package il.ac.bgu.se.bp.service;

import il.ac.bgu.se.bp.debugger.BPJsDebugger;
import il.ac.bgu.se.bp.rest.request.DebugRequest;
import il.ac.bgu.se.bp.rest.request.RunRequest;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;

public interface BPjsIDEService extends BPJsDebugger<BooleanResponse> {

    BooleanResponse subscribeUser(String sessionId, String userId);

    BooleanResponse run(RunRequest runRequest, String userId);

    BooleanResponse debug(DebugRequest debugRequest, String userId);

    BooleanResponse addBreakpoint(int lineNumber);

    BooleanResponse removeBreakpoint(int lineNumber);
}
