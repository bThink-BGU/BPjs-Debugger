package il.ac.bgu.se.bp.service;

import il.ac.bgu.se.bp.DummyDataRequest;
import il.ac.bgu.se.bp.ExecuteBPjsResponse;

public interface BPjsIDEService {

    ExecuteBPjsResponse run(DummyDataRequest code);
    ExecuteBPjsResponse debug(DummyDataRequest code);
    ExecuteBPjsResponse setBreakpoint(int lineNumber);
    ExecuteBPjsResponse removeBreakpoint(int lineNumber);
    ExecuteBPjsResponse continueRun();
    ExecuteBPjsResponse stepInto();
    ExecuteBPjsResponse stepOver();

}
