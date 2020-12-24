package il.ac.bgu.se.bp.service;

import il.ac.bgu.se.bp.DummyDataRequest;
import il.ac.bgu.se.bp.DummyDataResponse;
import il.ac.bgu.se.bp.logger.Logger;
import org.springframework.stereotype.Service;

@Service
public class BPjsIDEServiceImpl implements BPjsIDEService {

    private static final Logger logger = new Logger(BPjsIDEServiceImpl.class);

    @Override
    public DummyDataResponse run(DummyDataRequest code) {
        logger.info("received run request with code: {0}", code.toString());
        return new DummyDataResponse("RUN!");
    }

    @Override
    public DummyDataResponse debug(DummyDataRequest code) {
        logger.info("received debug request with code: {0}", code.toString());
        return new DummyDataResponse("DEBUG!");
    }
}
