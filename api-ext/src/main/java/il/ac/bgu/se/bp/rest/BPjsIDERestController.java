package il.ac.bgu.se.bp.rest;

import il.ac.bgu.se.bp.DummyDataRequest;
import il.ac.bgu.se.bp.ExecuteBPjsResponse;

public interface BPjsIDERestController {

    ExecuteBPjsResponse run(DummyDataRequest code);
    ExecuteBPjsResponse debug(DummyDataRequest code);
}
