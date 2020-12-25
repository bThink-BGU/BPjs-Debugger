package il.ac.bgu.se.bp.rest;

import il.ac.bgu.se.bp.DebugRequest;
import il.ac.bgu.se.bp.ExecuteBPjsResponse;

public interface BPjsIDERestController {

    ExecuteBPjsResponse run(DebugRequest code);
    ExecuteBPjsResponse debug(DebugRequest code);
}
