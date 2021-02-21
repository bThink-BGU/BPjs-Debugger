package il.ac.bgu.se.bp.debugger.commands;

import il.ac.bgu.se.bp.debugger.DebuggerCommand;
import il.ac.bgu.se.bp.debugger.DebuggerEngine;

import java.util.concurrent.FutureTask;

public class SetBreakpoint implements DebuggerCommand<FutureTask<String>, String> {
    private final boolean stopOnBreakpoint;
    private final int lineNumber;

    public SetBreakpoint(int lineNumber, boolean stopOnBreakpoint) {
        this.lineNumber = lineNumber;
        this.stopOnBreakpoint = stopOnBreakpoint;
    }

    @Override
    public FutureTask<String> applyCommand(DebuggerEngine<FutureTask<String>, String> debugger) {
        return new FutureTask<>(() -> debugger.setBreakpoint(lineNumber, stopOnBreakpoint));
    }
}