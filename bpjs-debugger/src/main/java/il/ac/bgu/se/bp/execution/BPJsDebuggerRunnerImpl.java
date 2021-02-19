package il.ac.bgu.se.bp.execution;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListenerAdapter;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionResult;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionStrategy;
import il.ac.bgu.se.bp.debugger.BPJsDebuggerRunner;
import il.ac.bgu.se.bp.debugger.DebuggerCommand;
import il.ac.bgu.se.bp.debugger.DebuggerOperations;
import il.ac.bgu.se.bp.debugger.commands.*;
import il.ac.bgu.se.bp.engine.DebuggerEngineImpl;
import il.ac.bgu.se.bp.logger.Logger;

import java.util.*;
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
    private boolean waitOnSync;
    private volatile boolean isSetup = false;
    private volatile boolean isStarted = false;

    public BPJsDebuggerRunnerImpl(String filename) {
        debuggerEngineImpl = new DebuggerEngineImpl(filename);
        bProg = new ResourceBProgram(filename);
    }

    @Override
    public void setup(Map<Integer, Boolean> breakpoints) {
        if(this.syncSnapshot == null)
            this.syncSnapshot = bProg.setup();
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
        if (!isSetup()) {
            setup(new HashMap<>());
        }
        setItStarted(true);
        new Thread(() -> {
            try {
                this.syncSnapshot = this.syncSnapshot.start(execSvc);
                System.out.println("GOT NEW SYNC STATE - First sync state");
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
                Optional<EventSelectionResult> eventOptional = eventSelectionStrategy.select(this.syncSnapshot, events);
                if(eventOptional.isPresent())
                {
                    BEvent event = eventSelectionStrategy.select(this.syncSnapshot, events).get().getEvent();
                    System.out.println(event);
                    this.syncSnapshot = this.syncSnapshot.triggerEvent(event, execSvc, new ArrayList<>());
                    System.out.println("GOT NEW SYNC STATE");
                }
                else{
                    System.out.println("Events queue is empty");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public FutureTask<String> continueRun() {
        return !isSetup() ? createResolvedFuture("setup is needed") :
            debuggerEngineImpl.addCommand(new Continue());
    }

    public FutureTask<String> stepInto() {
        return !isSetup() ? createResolvedFuture("setup is needed") :
            debuggerEngineImpl.addCommand(new StepInto());
    }

    public FutureTask<String> stepOver() {
        return !isSetup() ? createResolvedFuture("setup is needed") :
            debuggerEngineImpl.addCommand(new StepOver());
    }

    public FutureTask<String> stepOut() {
        return !isSetup() ? createResolvedFuture("setup is needed") :
            debuggerEngineImpl.addCommand(new StepOut());
    }

    @Override
    public FutureTask<String> setBreakpoint(int lineNumber, boolean stopOnBreakpoint) {
        return !isSetup() ? createResolvedFuture("setup is needed") :
                debuggerEngineImpl.addCommand(new SetBreakpoint(lineNumber, true));
    }

    public FutureTask<String> getVars() {
        return !isSetup() ? createResolvedFuture("setup is needed") :
            debuggerEngineImpl.addCommand(new GetVars());
    }

    public FutureTask<String> exit() {
        return  !isSetup() ? createResolvedFuture("setup is needed") :
                !isStarted() ? createResolvedFuture("The program has ended") :
                debuggerEngineImpl.addCommand(new Exit());
    }

    @Override
    public FutureTask<String> stop() {
        return  !isSetup() ? createResolvedFuture("setup is needed") :
            debuggerEngineImpl.addCommand(new Stop());
    }

    private FutureTask<String> createResolvedFuture(String result) {
        FutureTask<String> futureTask = new FutureTask<>(() -> result);
        futureTask.run();
        return futureTask;
    }
}
