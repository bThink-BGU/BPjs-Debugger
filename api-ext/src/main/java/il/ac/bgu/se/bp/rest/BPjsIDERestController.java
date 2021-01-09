package il.ac.bgu.se.bp.rest;

import il.ac.bgu.se.bp.DebugRequest;
import il.ac.bgu.se.bp.ExecuteBPjsResponse;
import il.ac.bgu.se.bp.debugger.AcknowledgedResponse;

public interface BPjsIDERestController {

    ExecuteBPjsResponse run(DebugRequest code);
    ExecuteBPjsResponse debug(DebugRequest code);

//    AcknowledgedResponse nextStep(NextStepRequest nextStepRequest);
}
