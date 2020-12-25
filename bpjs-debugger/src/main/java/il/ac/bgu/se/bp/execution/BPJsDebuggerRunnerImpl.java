package il.ac.bgu.se.bp.execution;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListenerAdapter;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionStrategy;
import il.ac.bgu.se.bp.debugger.BPJsDebuggerRunner;
import il.ac.bgu.se.bp.debugger.DebuggerCommand;
import il.ac.bgu.se.bp.debugger.DebuggerOperations;
import il.ac.bgu.se.bp.engine.DebuggerEngine;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Runs a {@link BProgram} in debug mode.
 */
public class BPJsDebuggerRunnerImpl implements BPJsDebuggerRunner<FutureTask<String>> {
    private BProgram bProg;
    private final DebuggerEngine debuggerEngine;
    ExecutorService execSvc = ExecutorServiceMaker.makeWithName("BPJsDebuggerRunner-" + 1);
    private BProgramSyncSnapshot syncSnapshot = null;
    private boolean started;

    public BPJsDebuggerRunnerImpl(String filename, int[] breakpoints) {
        debuggerEngine = new DebuggerEngine(filename);
        this.bProg = new ResourceBProgram(filename);
        bProg.setup();
        debuggerEngine.setup(breakpoints);
    }

    public void start() {
        BProgramRunner rnr = new BProgramRunner();
        rnr.addListener(new PrintBProgramRunnerListener());
        rnr.addListener(new BProgramRunnerListenerAdapter() {
            @Override
            public void ended(BProgram bp) {
                started = false;
            }
        });
        rnr.setBProgram(bProg);
        this.started= true;
        new Thread(rnr).start();
    }

    public void startSync() {
        try {
            this.syncSnapshot = this.bProg.getFirstSnapshot().start(execSvc);
            System.out.println(this.syncSnapshot);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void nextSync() {
        EventSelectionStrategy eventSelectionStrategy = this.bProg.getEventSelectionStrategy();
        Set<BEvent> events = eventSelectionStrategy.selectableEvents(this.syncSnapshot);
        try {
            this.syncSnapshot = this.syncSnapshot.triggerEvent(eventSelectionStrategy.select(this.syncSnapshot, events).get().getEvent(), execSvc, new ArrayList<>());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    public FutureTask<String> getVars() {
        return debuggerEngine.addCommand(new DebuggerCommand(DebuggerOperations.GET_VARS));
    }

    public FutureTask<String> exit() {
        if(this.started)
            return debuggerEngine.addCommand(new DebuggerCommand(DebuggerOperations.EXIT));
        else {
            FutureTask futureTask = new FutureTask<>(() -> "The program has ended");
            futureTask.run();
            return futureTask;
        }
    }
}
