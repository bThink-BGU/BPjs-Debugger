package il.ac.bgu.se.bp.service;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.se.bp.DummyDataRequest;
import il.ac.bgu.se.bp.ExecuteBPjsResponse;
import il.ac.bgu.se.bp.cache.BPjsIDECacheManager;
import il.ac.bgu.se.bp.debugger.BPJsDebuggerRunner;
import il.ac.bgu.se.bp.execution.BPJsDebuggerRunnerImpl;
import il.ac.bgu.se.bp.logger.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@EnableScheduling
public class BPjsIDEServiceImpl implements BPjsIDEService {

    private static final Logger logger = new Logger(BPjsIDEServiceImpl.class);

    @Autowired
    BPjsIDECacheManager bPjsIDECacheManager;



    //  todo: update userId time after each event injection

    @Override
    public ExecuteBPjsResponse run(DummyDataRequest code) {
        String newUserId = UUID.randomUUID().toString();
        BProgramRunner bProgramRunner = new BProgramRunner();

        bPjsIDECacheManager.addNewRunExecution(newUserId, bProgramRunner);
        bPjsIDECacheManager.updateLastOperationTime(newUserId);

        new Thread(bProgramRunner); //todo: fix

        logger.info("received run request with code: {0}", code.toString());
        return new ExecuteBPjsResponse(newUserId);
    }

    @Override
    public ExecuteBPjsResponse debug(DummyDataRequest code) {
        String newUserId = UUID.randomUUID().toString();
        BPJsDebuggerRunner bpProgramDebugger = new BPJsDebuggerRunnerImpl();

        bPjsIDECacheManager.addNewDebugExecution(newUserId, bpProgramDebugger);
        bPjsIDECacheManager.updateLastOperationTime(newUserId);

        new Thread(bpProgramDebugger::start);

        // todo: add userId -> socket mapping
        //  update userId time after each debug operation

        logger.info("received debug request with code: {0}", code.toString());
        return new ExecuteBPjsResponse(newUserId);
    }

    @Override
    public ExecuteBPjsResponse setBreakpoint(int lineNumber) {
        return null;
    }

    @Override
    public ExecuteBPjsResponse removeBreakpoint(int lineNumber) {
        return null;
    }

    @Override
    public ExecuteBPjsResponse continueRun() {
        return null;
    }

    @Override
    public ExecuteBPjsResponse stepInto() {
        return null;
    }

    @Override
    public ExecuteBPjsResponse stepOver() {
        return null;
    }
}
