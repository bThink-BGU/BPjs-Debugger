package il.ac.bgu.se.bp.debugger.commands;

import il.ac.bgu.se.bp.debugger.engine.DebuggerEngine;
import il.ac.bgu.se.bp.error.ErrorCode;
import il.ac.bgu.se.bp.execution.BPJsDebuggerImpl;
import il.ac.bgu.se.bp.logger.Logger;
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
            try{
                debugger.setBreakpoint(lineNumber, stopOnBreakpoint);
                return createSuccessResponse();
            }
            catch (IllegalArgumentException e ){
                Logger logger = new Logger(BPJsDebuggerImpl.class);
                logger.error("cant set breakpoint line {0} to {1} ", lineNumber, stopOnBreakpoint);
            }
        }
        return createErrorResponse(ErrorCode.BREAKPOINT_NOT_ALLOWED); //todo: add lineNumber

    }
}