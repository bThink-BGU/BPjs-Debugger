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
import il.ac.bgu.se.bp.engine.DebuggerEngineImpl;
import il.ac.bgu.se.bp.logger.Logger;

import java.util.*;
import java.util.concurrent.*;

import static java.util.Collections.reverseOrder;

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
    }

    @Override
    public void setup(Map<Integer, Boolean> breakpoints) {
        this.syncSnapshot = bProg.setup();
        debuggerEngineImpl.setupBreakpoint(breakpoints);
        setIsSetup(true);
        this.bProg.setWaitForExternalEvents(true);
    }

    //OLD METHOD TO RUN BPROG - JUST FOR REFERENCE
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

    /**
     * Start the bprog and get the first syncsnapshot.
     */
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
            Set<BEvent> possibleEvents = eventSelectionStrategy.selectableEvents(this.syncSnapshot);
            if (possibleEvents.isEmpty()) {
                if (this.bProg.isWaitForExternalEvents()) {
                    try{
                        BEvent next = this.bProg.takeExternalEvent(); // and now we wait for external event
                        if (next == null) {
                            return;
                        } else {
                            this.syncSnapshot.getExternalEvents().add(next);
                            possibleEvents = eventSelectionStrategy.selectableEvents(this.syncSnapshot);
                        }
                    }
                    catch (Exception e){
                        return;
                    }
                } else {
                    System.out.println("Not events wait for external events. termination?");
                }
            }

            System.out.println("all events:" + possibleEvents);
            System.out.println("External events:" +this.syncSnapshot.getExternalEvents());
            try {
                Optional<EventSelectionResult> eventOptional = eventSelectionStrategy.select(this.syncSnapshot, possibleEvents);
                if(eventOptional.isPresent())
                {
                    EventSelectionResult esr = eventOptional.get();
                    BEvent event = esr.getEvent();
                    System.out.println("Selected event: "+ event);

                    if ( ! esr.getIndicesToRemove().isEmpty() ) {
                        removeExternalEvents(esr);
                    }

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

    private void removeExternalEvents(EventSelectionResult esr) {
        // the event selection affected the external event queue.
        List<BEvent> updatedExternals = new ArrayList<>(this.syncSnapshot.getExternalEvents());
        esr.getIndicesToRemove().stream().sorted(reverseOrder())
                .forEach( idxObj -> updatedExternals.remove(idxObj.intValue()) );
        this.syncSnapshot = this.syncSnapshot.copyWith(updatedExternals);
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

    public FutureTask<String> addExternalEvent(String externalEvent){
        if (!isSetup()) {
            return createResolvedFuture("setup is needed");
        }
        this.bProg.enqueueExternalEvent(new BEvent(externalEvent));

        return createResolvedFuture("Added external event: "+ externalEvent );
    }

    private FutureTask<String> createResolvedFuture(String result) {
        FutureTask<String> futureTask = new FutureTask<>(() -> result);
        futureTask.run();
        return futureTask;
    }
}
