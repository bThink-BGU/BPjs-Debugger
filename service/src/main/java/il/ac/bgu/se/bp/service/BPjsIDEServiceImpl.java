package il.ac.bgu.se.bp.service;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.se.bp.debugger.BPJsDebugger;
import il.ac.bgu.se.bp.debugger.manage.DebuggerFactory;
import il.ac.bgu.se.bp.error.ErrorCode;
import il.ac.bgu.se.bp.logger.Logger;
import il.ac.bgu.se.bp.rest.request.*;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;
import il.ac.bgu.se.bp.service.code.SourceCodeHelper;
import il.ac.bgu.se.bp.service.manage.SessionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@EnableScheduling
public class BPjsIDEServiceImpl implements BPjsIDEService {

//    @Scheduled(fixedRate = 1000)
//    public void threadsCount() {
//        logger.info("THREADS COUNT: {0}", Thread.activeCount());
//    }

    private static final Logger logger = new Logger(BPjsIDEServiceImpl.class);

    @Autowired
    private SessionHandler<BProgramRunner> sessionHandler;

    @Autowired
    private SourceCodeHelper sourceCodeHelper;

    @Autowired
    private DebuggerFactory<BooleanResponse> debuggerFactory;

    @Override
    public BooleanResponse subscribeUser(String sessionId, String userId) {
        System.out.println("Received message from {1} with sessionId {2}" + ",," + userId + "," + sessionId);
        sessionHandler.addUser(sessionId, userId);
        return new BooleanResponse(true);
    }

    @Override
    public BooleanResponse run(RunRequest runRequest, String userId) {
        BProgramRunner bProgramRunner = new BProgramRunner();

        //todo
        sessionHandler.addNewRunExecution(userId, bProgramRunner, "filename");
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

        String filename = sourceCodeHelper.createCodeFile(debugRequest.getSourceCode());
        if (StringUtils.isEmpty(filename)) {
            return createErrorResponse(ErrorCode.INVALID_SOURCE_CODE);
        }

        logger.info("received debug request for user: {0}", userId);
        return handleNewDebugRequest(debugRequest, userId, filename);
    }

    private BooleanResponse handleNewDebugRequest(DebugRequest debugRequest, String userId, String filename) {
        BPJsDebugger<BooleanResponse> bpProgramDebugger = debuggerFactory.getBPJsDebugger(userId, filename);
        bpProgramDebugger.subscribe(sessionHandler);

        sessionHandler.addNewDebugExecution(userId, bpProgramDebugger, filename);
        sessionHandler.updateLastOperationTime(userId);

        Map<Integer, Boolean> breakpointsMap = debugRequest.getBreakpoints()
                .stream()
                .collect(Collectors.toMap(Function.identity(), b -> Boolean.TRUE));

        return bpProgramDebugger.startSync(breakpointsMap, debugRequest.isSkipBreakpointsToggle(), debugRequest.isSkipSyncStateToggle());
    }

    @Override
    public BooleanResponse setBreakpoint(String userId, SetBreakpointRequest setBreakpointRequest) {
        if (setBreakpointRequest == null) {
            return createErrorResponse(ErrorCode.INVALID_REQUEST);
        }

        BPJsDebugger<BooleanResponse> bpJsDebugger = sessionHandler.getBPjsDebuggerByUser(userId);
        if (bpJsDebugger == null) {
            return createErrorResponse(ErrorCode.UNKNOWN_USER);
        }

        sessionHandler.updateLastOperationTime(userId);
        return bpJsDebugger.setBreakpoint(setBreakpointRequest.getLineNumber(), setBreakpointRequest.isStopOnBreakpoint());
    }

    @Override
    public BooleanResponse toggleMuteBreakpoints(String userId, ToggleBreakpointsRequest toggleBreakPointStatus) {
        if (toggleBreakPointStatus == null) {
            return createErrorResponse(ErrorCode.INVALID_REQUEST);
        }

        BPJsDebugger<BooleanResponse> bpJsDebugger = sessionHandler.getBPjsDebuggerByUser(userId);
        if (bpJsDebugger == null) {
            return createErrorResponse(ErrorCode.UNKNOWN_USER);
        }

        sessionHandler.updateLastOperationTime(userId);
        return bpJsDebugger.toggleMuteBreakpoints(toggleBreakPointStatus.isSkipBreakpoints());
    }

    @Override
    public BooleanResponse toggleMuteSyncPoints(String userId, ToggleSyncStatesRequest toggleMuteSyncPoints) {
        if (toggleMuteSyncPoints == null) {
            return createErrorResponse(ErrorCode.INVALID_REQUEST);
        }

        BPJsDebugger<BooleanResponse> bpJsDebugger = sessionHandler.getBPjsDebuggerByUser(userId);
        if (bpJsDebugger == null) {
            return createErrorResponse(ErrorCode.UNKNOWN_USER);
        }

        sessionHandler.updateLastOperationTime(userId);
        return bpJsDebugger.toggleMuteSyncPoints(toggleMuteSyncPoints.isSkipSyncStates());
    }

    @Override
    public BooleanResponse stop(String userId) {
        BPJsDebugger<BooleanResponse> bpJsDebugger = sessionHandler.getBPjsDebuggerByUser(userId);
        if (bpJsDebugger == null) {
            return createErrorResponse(ErrorCode.UNKNOWN_USER);
        }
        sessionHandler.updateLastOperationTime(userId);
        return bpJsDebugger.stop();
    }

    @Override
    public BooleanResponse stepOut(String userId) {
        BPJsDebugger<BooleanResponse> bpJsDebugger = sessionHandler.getBPjsDebuggerByUser(userId);
        if (bpJsDebugger == null) {
            return createErrorResponse(ErrorCode.UNKNOWN_USER);
        }
        sessionHandler.updateLastOperationTime(userId);
        return bpJsDebugger.stepOut();
    }

    @Override
    public BooleanResponse stepInto(String userId) {
        BPJsDebugger<BooleanResponse> bpJsDebugger = sessionHandler.getBPjsDebuggerByUser(userId);
        if (bpJsDebugger == null) {
            return createErrorResponse(ErrorCode.UNKNOWN_USER);
        }
        sessionHandler.updateLastOperationTime(userId);
        return bpJsDebugger.stepInto();
    }

    @Override
    public BooleanResponse stepOver(String userId) {
        BPJsDebugger<BooleanResponse> bpJsDebugger = sessionHandler.getBPjsDebuggerByUser(userId);
        if (bpJsDebugger == null) {
            return createErrorResponse(ErrorCode.UNKNOWN_USER);
        }
        sessionHandler.updateLastOperationTime(userId);
        return bpJsDebugger.stepOver();
    }

    @Override
    public BooleanResponse continueRun(String userId) {
        BPJsDebugger<BooleanResponse> bpJsDebugger = sessionHandler.getBPjsDebuggerByUser(userId);
        if (bpJsDebugger == null) {
            return createErrorResponse(ErrorCode.UNKNOWN_USER);
        }
        sessionHandler.updateLastOperationTime(userId);
        return bpJsDebugger.continueRun();
    }

    @Override
    public BooleanResponse nextSync(String userId) {
        BPJsDebugger<BooleanResponse> bpJsDebugger = sessionHandler.getBPjsDebuggerByUser(userId);
        if (bpJsDebugger == null) {
            return createErrorResponse(ErrorCode.UNKNOWN_USER);
        }
        sessionHandler.updateLastOperationTime(userId);
        return bpJsDebugger.nextSync();
    }

    @Override
    public BooleanResponse externalEvent(String userId, ExternalEventRequest externalEventRequest) {
        if (externalEventRequest == null || StringUtils.isEmpty(externalEventRequest.getExternalEvent())) {
            return createErrorResponse(ErrorCode.INVALID_REQUEST);
        }

        BPJsDebugger<BooleanResponse> bpJsDebugger = sessionHandler.getBPjsDebuggerByUser(userId);
        if (bpJsDebugger == null) {
            return createErrorResponse(ErrorCode.UNKNOWN_USER);
        }

        sessionHandler.updateLastOperationTime(userId);
        String externalEvent = externalEventRequest.getExternalEvent();
        return externalEventRequest.isAddEvent() ? bpJsDebugger.addExternalEvent(externalEvent) :
                bpJsDebugger.removeExternalEvent(externalEvent);
    }

    @Override
    public BooleanResponse setSyncSnapshot(String userId, SetSyncSnapshotRequest setSyncSnapshotRequest) {
        if (setSyncSnapshotRequest == null) {
            return createErrorResponse(ErrorCode.INVALID_REQUEST);
        }

        BPJsDebugger<BooleanResponse> bpJsDebugger = sessionHandler.getBPjsDebuggerByUser(userId);
        if (bpJsDebugger == null) {
            return createErrorResponse(ErrorCode.UNKNOWN_USER);
        }

        sessionHandler.updateLastOperationTime(userId);
        long snapShotTime = setSyncSnapshotRequest.getSnapShotTime();
        return bpJsDebugger.setSyncSnapshot(snapShotTime);
    }

    private BooleanResponse createErrorResponse(ErrorCode errorCode) {
        return new BooleanResponse(false, errorCode);
    }

    private boolean validateRequest(RunRequest runRequest) {
        return !StringUtils.isEmpty(runRequest.getSourceCode());
    }

}
