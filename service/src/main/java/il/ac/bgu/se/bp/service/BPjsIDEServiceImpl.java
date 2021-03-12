package il.ac.bgu.se.bp.service;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.se.bp.DebugRequest;
import il.ac.bgu.se.bp.ExecuteBPjsResponse;
import il.ac.bgu.se.bp.cache.BPjsIDECacheManager;
import il.ac.bgu.se.bp.debugger.BPJsDebugger;
import il.ac.bgu.se.bp.execution.BPJsDebuggerImpl;
import il.ac.bgu.se.bp.logger.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BPjsIDEServiceImpl implements BPjsIDEService {

    private static final Logger logger = new Logger(BPjsIDEServiceImpl.class);

    @Autowired
    BPjsIDECacheManager bPjsIDECacheManager;

    //  todo: update userId time after each event injection

    @Override
    public ExecuteBPjsResponse run(DebugRequest code) {
        String newUserId = UUID.randomUUID().toString();
        BProgramRunner bProgramRunner = new BProgramRunner();

        bPjsIDECacheManager.addNewRunExecution(newUserId, bProgramRunner);
        bPjsIDECacheManager.updateLastOperationTime(newUserId);

//        new Thread(bProgramRunner); //todo: fix   ////aaaaaaaa

        logger.info("received run request with code: {0}", code.toString());
        return new ExecuteBPjsResponse(newUserId);
    }

    @Override
    public ExecuteBPjsResponse debug(DebugRequest code) {
        String newUserId = UUID.randomUUID().toString();
        final String filename = "BPJSDebuggerTest.js";

        BPJsDebugger bpProgramDebugger = new BPJsDebuggerImpl(filename, () -> true, a -> null);
//        bpProgramDebugger.setup(code.getBreakpoints());

        bPjsIDECacheManager.addNewDebugExecution(newUserId, bpProgramDebugger);
        bPjsIDECacheManager.updateLastOperationTime(newUserId);

//        new Thread(bpProgramDebugger::start);

        // todo: add userId -> socket mapping
        //  update userId time after each debug operation

        logger.info("received debug request with code: {0}", code.toString());
        return new ExecuteBPjsResponse(newUserId);
    }

    @Override
    public ExecuteBPjsResponse addBreakpoint(int lineNumber) {
        return setBreakpoint(lineNumber, true);
    }

    @Override
    public ExecuteBPjsResponse removeBreakpoint(int lineNumber) {
        return setBreakpoint(lineNumber, false);
    }

    @Override
    public ExecuteBPjsResponse continueRun() {
        return null;
    }

    @Override
    public ExecuteBPjsResponse toggleMuteBreakpoints(boolean toggleBreakPointStatus) {
        return null;
    }

    @Override
    public ExecuteBPjsResponse getState() {
        return null;
    }

    @Override
    public ExecuteBPjsResponse setBreakpoint(int lineNumber, boolean stopOnBreakpoint) {
        return null;
    }

    @Override
    public ExecuteBPjsResponse stop() {
        return null;
    }

    @Override
    public ExecuteBPjsResponse stepOut() {
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
