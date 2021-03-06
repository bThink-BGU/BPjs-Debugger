package il.ac.bgu.se.bp.debugger.commands;

import il.ac.bgu.se.bp.debugger.engine.DebuggerEngine;
import il.ac.bgu.se.bp.error.ErrorCode;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;

import static il.ac.bgu.se.bp.utils.ResponseHelper.createErrorResponse;
import static il.ac.bgu.se.bp.utils.ResponseHelper.createSuccessResponse;

public class SetBreakpoint implements DebuggerCommand {
    private final boolean stopOnBreakpoint;
    private final int lineNumber;

    public SetBreakpoint(int lineNumber, boolean stopOnBreakpoint) {
        this.lineNumber = lineNumber;
        this.stopOnBreakpoint = stopOnBreakpoint;
    }

    @Override
    public BooleanResponse applyCommand(DebuggerEngine debugger) {
        if (debugger.isBreakpointAllowed(lineNumber)) {
            debugger.setBreakpoint(lineNumber, stopOnBreakpoint);
            return createSuccessResponse();
        }

        return createErrorResponse(ErrorCode.BREAKPOINT_NOT_ALLOWED); //todo: add lineNumber
    }
}