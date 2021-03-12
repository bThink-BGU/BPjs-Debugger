package il.ac.bgu.se.bp.service;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.se.bp.ExecuteBPjsResponse;
import il.ac.bgu.se.bp.cache.BPjsIDECacheManager;
import il.ac.bgu.se.bp.debugger.BPJsDebugger;
import il.ac.bgu.se.bp.execution.BPJsDebuggerImpl;
import il.ac.bgu.se.bp.logger.Logger;
import il.ac.bgu.se.bp.rest.request.DebugRequest;
import il.ac.bgu.se.bp.rest.request.RunRequest;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;
import il.ac.bgu.se.bp.rest.response.GetSyncSnapshotsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class BPjsIDEServiceImpl implements BPjsIDEService {

    private static final Logger logger = new Logger(BPjsIDEServiceImpl.class);

    @Autowired
    private BPjsIDECacheManager bPjsIDECacheManager;

    //  todo: update userId time after each event injection

    @Override
    public BooleanResponse subscribeUser(String sessionId, String userId) {
        System.out.println("Received message from {1} with sessionId {2}" + ",," + userId + "," + sessionId);

        //todo: see reference: GreetingService, GreetingService.sendMessage

        return new BooleanResponse(true);
    }

    @Override
    public ExecuteBPjsResponse run(RunRequest code) {
        String newUserId = UUID.randomUUID().toString();
        BProgramRunner bProgramRunner = new BProgramRunner();

        bPjsIDECacheManager.addNewRunExecution(newUserId, bProgramRunner);
        bPjsIDECacheManager.updateLastOperationTime(newUserId);

//        new Thread(bProgramRunner); //todo: fix   ////aaaaaaaaa

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
    public BooleanResponse addBreakpoint(int lineNumber) {
        return setBreakpoint(lineNumber, true);
    }

    @Override
    public BooleanResponse removeBreakpoint(int lineNumber) {
        return setBreakpoint(lineNumber, false);
    }

    @Override
    public BooleanResponse setup(Map<Integer, Boolean> breakpoints, boolean isSkipSyncPoints) {
        return null;
    }

    @Override
    public boolean isSetup() {
        return false;
    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public BooleanResponse addExternalEvent(String externalEvent) {
        return null;
    }

    @Override
    public BooleanResponse removeExternalEvent(String externalEvent) {
        return null;
    }

    @Override
    public BooleanResponse setWaitForExternalEvents(boolean shouldWait) {
        return null;
    }

    @Override
    public BooleanResponse startSync(boolean isSkipSyncPoints) {
        return null;
    }

    @Override
    public BooleanResponse nextSync() {
        return null;
    }

    @Override
    public BooleanResponse setIsSkipSyncPoints(boolean isSkipSyncPoints) {
        return null;
    }

    @Override
    public GetSyncSnapshotsResponse getSyncSnapshotsHistory() {
        return null;
    }

    @Override
    public BooleanResponse setSyncSnapshots(long snapShotTime) {
        return null;
    }

    @Override
    public BooleanResponse setBreakpoint(int lineNumber, boolean stopOnBreakpoint) {
        return null;
    }

    @Override
    public BooleanResponse stop() {
        return null;
    }

    @Override
    public BooleanResponse stepOut() {
        return null;
    }

    @Override
    public BooleanResponse stepInto() {
        return null;
    }

    @Override
    public BooleanResponse stepOver() {
        return null;
    }

    @Override
    public BooleanResponse continueRun() {
        return null;
    }

    @Override
    public BooleanResponse toggleMuteBreakpoints(boolean toggleBreakPointStatus) {
        return null;
    }

    @Override
    public BooleanResponse getState() {
        return null;
    }
}
