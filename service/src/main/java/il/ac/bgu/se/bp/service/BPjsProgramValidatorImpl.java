package il.ac.bgu.se.bp.service;

import il.ac.bgu.se.bp.debugger.BPJsDebugger;
import il.ac.bgu.se.bp.debugger.RunnerState;
import il.ac.bgu.se.bp.debugger.runner.BPjsProgramValidator;
import il.ac.bgu.se.bp.error.ErrorCode;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;
import org.springframework.stereotype.Component;


import static il.ac.bgu.se.bp.utils.ResponseHelper.createErrorResponse;
import static il.ac.bgu.se.bp.utils.ResponseHelper.createSuccessResponse;

@Component
public class BPjsProgramValidatorImpl implements BPjsProgramValidator<BPJsDebugger> {

    @Override
    public BooleanResponse validateNextSync(BPJsDebugger bProg) {
        RunnerState state = bProg.getDebuggerState();

        if (state.getDebuggerState() == RunnerState.State.WAITING_FOR_EXTERNAL_EVENT)
            return createErrorResponse(ErrorCode.WAITING_FOR_EXTERNAL_EVENT);
        else if (state.getDebuggerState() == RunnerState.State.JS_DEBUG)
            return createErrorResponse(ErrorCode.NOT_IN_BP_SYNC_STATE);
        else if (state.getDebuggerState() == RunnerState.State.RUNNING)
            return createErrorResponse(ErrorCode.ALREADY_RUNNING);
        else if (!bProg.isStarted())
            return createErrorResponse(ErrorCode.NOT_STARTED);

        return createSuccessResponse();
    }

    @Override
    public BooleanResponse validateContinueRun(BPJsDebugger bProg) {
        RunnerState state = bProg.getDebuggerState();

        if (!bProg.isSetup())
            return createErrorResponse(ErrorCode.SETUP_REQUIRED);
        if (state.getDebuggerState() != RunnerState.State.JS_DEBUG) {
            return createErrorResponse(ErrorCode.NOT_IN_JS_DEBUG_STATE);
        }

        return createSuccessResponse();
    }
}
