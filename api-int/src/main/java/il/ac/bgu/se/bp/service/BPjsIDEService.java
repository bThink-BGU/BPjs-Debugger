package il.ac.bgu.se.bp.service;

import il.ac.bgu.se.bp.DebugRequest;
import il.ac.bgu.se.bp.ExecuteBPjsResponse;
import il.ac.bgu.se.bp.debugger.Debugger;

public interface BPjsIDEService extends Debugger<ExecuteBPjsResponse> {
    ExecuteBPjsResponse run(DebugRequest code);
    ExecuteBPjsResponse debug(DebugRequest code);
    ExecuteBPjsResponse addBreakpoint(int lineNumber);
    ExecuteBPjsResponse removeBreakpoint(int lineNumber);
}
