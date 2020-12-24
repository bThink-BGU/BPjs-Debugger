package il.ac.bgu.se.bp.service;

import il.ac.bgu.se.bp.DummyDataRequest;
import il.ac.bgu.se.bp.DummyDataResponse;

public interface BPjsIDEService {

    DummyDataResponse run(DummyDataRequest code);
    DummyDataResponse debug(DummyDataRequest code);

}
