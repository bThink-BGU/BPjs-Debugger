package il.ac.bgu.se.bp.service;

import il.ac.bgu.se.bp.DebugRequest;
import il.ac.bgu.se.bp.ExecuteBPjsResponse;

public interface BPjsIDEService {

    ExecuteBPjsResponse run(DebugRequest code);
    ExecuteBPjsResponse debug(DebugRequest code);
    ExecuteBPjsResponse setBreakpoint(int lineNumber);
    ExecuteBPjsResponse removeBreakpoint(int lineNumber);
    ExecuteBPjsResponse continueRun();
    ExecuteBPjsResponse stepInto();
    ExecuteBPjsResponse stepOver();

}
