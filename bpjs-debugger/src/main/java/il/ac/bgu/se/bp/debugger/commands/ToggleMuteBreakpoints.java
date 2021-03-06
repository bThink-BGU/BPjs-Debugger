package il.ac.bgu.se.bp.debugger.commands;

import il.ac.bgu.se.bp.debugger.engine.DebuggerEngine;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;

import static il.ac.bgu.se.bp.utils.FutureHelper.createSuccessResult;

public class ToggleMuteBreakpoints implements DebuggerCommand {

    private final boolean toggleBreakPointStatus;

    public ToggleMuteBreakpoints(boolean toggleBreakPointStatus) {
        this.toggleBreakPointStatus = toggleBreakPointStatus;
    }

    @Override
    public BooleanResponse applyCommand(DebuggerEngine debugger) {
        debugger.toggleMuteBreakpoints(toggleBreakPointStatus);
        return createSuccessResult();
    }
}