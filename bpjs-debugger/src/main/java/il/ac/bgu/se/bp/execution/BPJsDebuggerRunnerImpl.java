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
import il.ac.bgu.se.bp.engine.DebuggerEngineImpl;
import il.ac.bgu.se.bp.logger.Logger;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Runs a {@link BProgram} in debug mode.
 */
public class BPJsDebuggerRunnerImpl implements BPJsDebuggerRunner<FutureTask<String>> {
    private final Logger logger = new Logger(BPJsDebuggerRunnerImpl.class);

    private final BProgram bProg;
    private final DebuggerEngineImpl debuggerEngineImpl;
    private final ExecutorService execSvc = ExecutorServiceMaker.makeWithName("BPJsDebuggerRunner-" + 1);
    private BProgramSyncSnapshot syncSnapshot = null;
    private volatile boolean isSetup = false;
    private volatile boolean isStarted = false;

    public BPJsDebuggerRunnerImpl(String filename) {
        debuggerEngineImpl = new DebuggerEngineImpl(filename);
        bProg = new ResourceBProgram(filename);
        this.syncSnapshot = bProg.setup();
    }

    @Override
    public void setup(Map<Integer, Boolean> breakpoints) {
        if (isSetup()) {
            debuggerEngineImpl.setupBreakpoint(breakpoints);
            return;
        }

        bProg.setup();
        debuggerEngineImpl.setupBreakpoint(breakpoints);
        setIsSetup(true);
    }

    @Override
    public void start(Map<Integer, Boolean> breakpoints) {
        if (!isSetup) {
            setup(breakpoints);
            return;
        }
        BProgramRunner rnr = new BProgramRunner();
        rnr.addListener(new PrintBProgramRunnerListener());
        rnr.addListener(new BProgramRunnerListenerAdapter() {
            @Override
            public void ended(BProgram bp) {
                setItStarted(false);
            }
        });
        rnr.setBProgram(bProg);
        setItStarted(true);
        new Thread(rnr).start();
    }

    private void setItStarted(boolean isStarted) {
        this.isStarted = isStarted;
    }

    private synchronized void setIsSetup(boolean isSetup) {
        this.isSetup = isSetup;
    }

    @Override
    public synchronized boolean isSetup() {
        return isSetup;
    }

    @Override
    public synchronized boolean isStarted() {
        return isStarted;
    }

    public void startSync() {
        setIsSetup(true);
        setItStarted(true);
        new Thread(() -> {
            try {
                this.syncSnapshot = this.bProg.getFirstSnapshot().start(execSvc);
                System.out.println("GOT NEW SYNC STATE");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void nextSync() {
        new Thread(() -> {
            EventSelectionStrategy eventSelectionStrategy = this.bProg.getEventSelectionStrategy();
            Set<BEvent> events = eventSelectionStrategy.selectableEvents(this.syncSnapshot);
            try {
                BEvent event = eventSelectionStrategy.select(this.syncSnapshot, events).get().getEvent();
                System.out.println(event);
                this.syncSnapshot = this.syncSnapshot.triggerEvent(event, execSvc, new ArrayList<>());
                System.out.println("GOT NEW SYNC STATE");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();


    }

    public FutureTask<String> setBreakpoint(int lineNumber) {
        if (!isSetup()) {
            return createResolvedFuture("setup is needed");
        }
        return debuggerEngineImpl.addCommand(new DebuggerCommand(DebuggerOperations.SET_BREAKPOINT, lineNumber));
    }

    public FutureTask<String> removeBreakpoint(int lineNumber) {
        if (!isSetup()) {
            return createResolvedFuture("setup is needed");
        }
        return debuggerEngineImpl.addCommand(new DebuggerCommand(DebuggerOperations.REMOVE_BREAKPOINT, lineNumber));
    }

    public FutureTask<String> continueRun() {
        if (!isSetup()) {
            return createResolvedFuture("setup is needed");
        }
        return debuggerEngineImpl.addCommand(new DebuggerCommand(DebuggerOperations.CONTINUE));
    }

    public FutureTask<String> stepInto() {
        if (!isSetup()) {
            return createResolvedFuture("setup is needed");
        }
        return debuggerEngineImpl.addCommand(new DebuggerCommand(DebuggerOperations.STEP_INTO));
    }

    public FutureTask<String> stepOver() {
        if (!isSetup()) {
            return createResolvedFuture("setup is needed");
        }
        return debuggerEngineImpl.addCommand(new DebuggerCommand(DebuggerOperations.STEP_OVER));
    }

    public FutureTask<String> stepOut() {
        if (!isSetup()) {
            return createResolvedFuture("setup is needed");
        }
        return debuggerEngineImpl.addCommand(new DebuggerCommand(DebuggerOperations.STEP_OUT));
    }

    public FutureTask<String> getVars() {
        if (!isSetup()) {
            return createResolvedFuture("setup is needed");
        }
        return debuggerEngineImpl.addCommand(new DebuggerCommand(DebuggerOperations.GET_VARS));
    }

    public FutureTask<String> exit() {
        if (!isSetup()) {
            return createResolvedFuture("setup is needed");
        }
        else if (!isStarted())
            return createResolvedFuture("The program has ended");
        else
            return debuggerEngineImpl.addCommand(new DebuggerCommand(DebuggerOperations.EXIT));
    }

    private FutureTask<String> createResolvedFuture(String result) {
        FutureTask<String> futureTask = new FutureTask<>(() -> result);
        futureTask.run();
        return futureTask;
    }
}
