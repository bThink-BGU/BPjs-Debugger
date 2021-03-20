package il.ac.bgu.se.bp.service;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.se.bp.debugger.BPJsDebugger;
import il.ac.bgu.se.bp.debugger.runner.BPjsProgramExecutor;
import il.ac.bgu.se.bp.debugger.runner.SessionHandler;
import il.ac.bgu.se.bp.error.ErrorCode;
import il.ac.bgu.se.bp.execution.BPJsDebuggerImpl;
import il.ac.bgu.se.bp.logger.Logger;
import il.ac.bgu.se.bp.rest.request.DebugRequest;
import il.ac.bgu.se.bp.rest.request.RunRequest;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;
import il.ac.bgu.se.bp.rest.response.GetSyncSnapshotsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.util.Map;

import static il.ac.bgu.se.bp.utils.ResponseHelper.createSuccessResponse;

@Service
public class BPjsIDEServiceImpl implements BPjsIDEService {

    private static final Logger logger = new Logger(BPjsIDEServiceImpl.class);

    @Autowired
    private SessionHandler<BProgramRunner> sessionHandler;

    @Autowired
    private BPjsProgramExecutor<BPJsDebugger> bPjsProgramExecutor;

    @Override
    public BooleanResponse subscribeUser(String sessionId, String userId) {
        System.out.println("Received message from {1} with sessionId {2}" + ",," + userId + "," + sessionId);
        sessionHandler.addUser(sessionId, userId);
        return new BooleanResponse(true);
    }

    @Override
    public BooleanResponse run(RunRequest runRequest, String userId) {
        BProgramRunner bProgramRunner = new BProgramRunner();

        sessionHandler.addNewRunExecution(userId, bProgramRunner);
        sessionHandler.updateLastOperationTime(userId);


        logger.info("received run request with code: {0}", bProgramRunner.toString());
        return new BooleanResponse(true);
    }

    @Override
    public BooleanResponse debug(DebugRequest debugRequest, String userId) {
        if (!validateRequest(debugRequest)) {
            return createErrorResponse(ErrorCode.INVALID_REQUEST);
        }

        if (!sessionHandler.validateUserId(userId)) {
            return createErrorResponse(ErrorCode.UNKNOWN_USER);
        }

        String filepath = "BPJSDebuggerForTesting.js"; //createCodeFile(debugRequest.getSourceCode());
        if (StringUtils.isEmpty(filepath)) {
            return createErrorResponse(ErrorCode.INVALID_SOURCE_CODE);
        }

        logger.info("received debug request for user: {0}", userId);
        return handleDebugRequest(debugRequest, userId, filepath);
    }

    private BooleanResponse handleDebugRequest(DebugRequest debugRequest, String userId, String filepath) {
        BPJsDebuggerImpl bpProgramDebugger = new BPJsDebuggerImpl(userId, filepath);
        bpProgramDebugger.subscribe(sessionHandler);

        sessionHandler.addNewDebugExecution(userId, bpProgramDebugger);
        sessionHandler.updateLastOperationTime(userId);

        bPjsProgramExecutor.debugProgram(bpProgramDebugger, debugRequest.getBreakpoints(),
                debugRequest.isSkipBreakpointsToggle(), debugRequest.isSkipSyncStateToggle());

        return createSuccessResponse();
    }

    private BooleanResponse createErrorResponse(ErrorCode errorCode) {
        return new BooleanResponse(false, errorCode);
    }

    private boolean validateRequest(RunRequest runRequest) {
        return !StringUtils.isEmpty(runRequest.getSourceCode());
    }

    private String createCodeFile(String sourceCode) {
        return null;
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
    public BooleanResponse setup(Map<Integer, Boolean> breakpoints, boolean isSkipBreakpoints, boolean isSkipSyncPoints) {
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
    public BooleanResponse startSync(boolean isSkipBreakpoints, boolean isSkipSyncPoints) {
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
