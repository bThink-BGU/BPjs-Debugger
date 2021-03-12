package il.ac.bgu.se.bp.service;

import il.ac.bgu.se.bp.ExecuteBPjsResponse;
import il.ac.bgu.se.bp.debugger.BPJsDebugger;
import il.ac.bgu.se.bp.rest.request.DebugRequest;
import il.ac.bgu.se.bp.rest.request.RunRequest;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;

public interface BPjsIDEService extends BPJsDebugger<BooleanResponse> {

    BooleanResponse subscribeUser(String sessionId, String userId);

    ExecuteBPjsResponse run(RunRequest code);

    ExecuteBPjsResponse debug(DebugRequest code);

    BooleanResponse addBreakpoint(int lineNumber);

    BooleanResponse removeBreakpoint(int lineNumber);
}
