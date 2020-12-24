package il.ac.bgu.se.bp.execution;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.se.bp.debugger.BPJsDebuggerRunner;
import il.ac.bgu.se.bp.debugger.DebuggerCommand;
import il.ac.bgu.se.bp.debugger.DebuggerOperations;
import il.ac.bgu.se.bp.engine.DebuggerEngine;

import java.util.concurrent.*;

/**
 * Runs a {@link BProgram} in debug mode.
 */
public class BPJsDebuggerRunnerImpl implements BPJsDebuggerRunner<FutureTask<String>> {
    private final DebuggerEngine debuggerEngine;
    private final String filename = "BPJSDebuggerTest.js";

    public BPJsDebuggerRunnerImpl() {
        debuggerEngine = new DebuggerEngine(filename);
    }

    public void start() {
        final BProgram bProg = new ResourceBProgram(filename);
        BProgramRunner rnr = new BProgramRunner();
        rnr.addListener(new PrintBProgramRunnerListener());
        rnr.setBProgram(bProg);
        bProg.setup();
        debuggerEngine.setup();
        new Thread(rnr).start();
    }

    public FutureTask<String> setBreakpoint(int lineNumber) {
        return debuggerEngine.addCommand(new DebuggerCommand(DebuggerOperations.SET_BREAKPOINT, lineNumber));
    }

    public FutureTask<String> removeBreakpoint(int lineNumber) {
        return debuggerEngine.addCommand(new DebuggerCommand(DebuggerOperations.REMOVE_BREAKPOINT, lineNumber));
    }

    public FutureTask<String> continueRun() {
        return debuggerEngine.addCommand(new DebuggerCommand(DebuggerOperations.CONTINUE));
    }

    public FutureTask<String> stepInto() {
        return debuggerEngine.addCommand(new DebuggerCommand(DebuggerOperations.STEP_INTO));
    }

    public FutureTask<String> stepOver() {
        return debuggerEngine.addCommand(new DebuggerCommand(DebuggerOperations.STEP_OVER));
    }

    public FutureTask<String> stepOut() {
        return debuggerEngine.addCommand(new DebuggerCommand(DebuggerOperations.STEP_OUT));
    }

}
