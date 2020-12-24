package il.ac.bgu.se.bp.rest;

import il.ac.bgu.se.bp.DummyDataRequest;
import il.ac.bgu.se.bp.DummyDataResponse;

public interface BPjsIDERestController {

    DummyDataResponse run(DummyDataRequest code);
    DummyDataResponse debug(DummyDataRequest code);
}
