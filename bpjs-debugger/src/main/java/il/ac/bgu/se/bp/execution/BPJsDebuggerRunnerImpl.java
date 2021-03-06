package il.ac.bgu.se.bp.execution;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListenerAdapter;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.*;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionResult;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionStrategy;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;
import il.ac.bgu.se.bp.debugger.BPJsDebuggerRunner;
import il.ac.bgu.se.bp.debugger.commands.*;
import il.ac.bgu.se.bp.debugger.engine.BPDebuggerState;
import il.ac.bgu.se.bp.debugger.engine.DebuggerEngineImpl;
import il.ac.bgu.se.bp.debugger.engine.SyncSnapshotHolder;
import il.ac.bgu.se.bp.debugger.engine.SyncSnapshotHolderImpl;
import il.ac.bgu.se.bp.logger.Logger;
import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.debugger.Dim;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Collections.reverseOrder;

/**
 * Runs a {@link BProgram} in debug mode.
 */
public class BPJsDebuggerRunnerImpl implements BPJsDebuggerRunner<FutureTask<String>> {
    private final static AtomicInteger debuggerId = new AtomicInteger(0);
    private final Logger logger = new Logger(BPJsDebuggerRunnerImpl.class);
    private final BProgram bProg;
    private final DebuggerEngineImpl debuggerEngineImpl;
    private final ExecutorService execSvc = ExecutorServiceMaker.makeWithName("BPJsDebuggerRunner-" + debuggerId.incrementAndGet());
    private BProgramSyncSnapshot syncSnapshot = null;
    private volatile boolean isSetup = false;
    private volatile boolean isStarted = false;
    private volatile boolean isSkipSyncPoints = false;
    private RunnerState state = new RunnerState();
    private LinkedList<Thread> runningThreads;
    private final Callable onExitInterrupt;
    private final SyncSnapshotHolder<BProgramSyncSnapshot, BEvent> syncSnapshotHolder;

    public BPJsDebuggerRunnerImpl(String filename, Callable onExitInterrupt) {
        this.onExitInterrupt = onExitInterrupt;
        runningThreads = new LinkedList<>();
        debuggerEngineImpl = new DebuggerEngineImpl(filename, state);
        bProg = new ResourceBProgram(filename);
        syncSnapshotHolder = new SyncSnapshotHolderImpl();
    }

    @Override
    public void setup(Map<Integer, Boolean> breakpoints, boolean isSkipSyncPoints) {
        syncSnapshot = bProg.setup();
        setIsSkipSyncPoints(isSkipSyncPoints);
        debuggerEngineImpl.setupBreakpoint(breakpoints);
        debuggerEngineImpl.setSyncSnapshot(syncSnapshot);
        setIsSetup(true);
//        this.bProg.setWaitForExternalEvents(true);        //todo: add wait for external event toggle
    }


    @Override
    public synchronized FutureTask<String> setIsSkipSyncPoints(boolean isSkipSyncPoints) {
        this.isSkipSyncPoints = isSkipSyncPoints;
        return createResolvedFuture("isSkipSyncPoints set to: " + isSkipSyncPoints);
    }

    @Override
    public FutureTask<String> getSyncSnapshotsHistory() {
        return createResolvedFuture(syncSnapshotHolder.getAllSyncSnapshots().toString());
    }

    @Override
    public FutureTask<String> setSyncSnapshots(long snapShotTime) {
        BProgramSyncSnapshot newSnapshot = syncSnapshotHolder.popKey(snapShotTime);
        if (newSnapshot == null) {
            return createResolvedFuture("failed replacing snapshot");
        }

        syncSnapshot = newSnapshot;
        nextSync();
        return createResolvedFuture("replaced snapshot");
    }

    private synchronized void setItStarted(boolean isStarted) {
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
    public FutureTask<String> startSync(boolean isSkipSyncPoints) {
        if (!isSetup()) {
            setup(new HashMap<>(), isSkipSyncPoints);
        }
        setItStarted(true);
        Thread startSyncThread = new Thread(() -> {
            try {
                syncSnapshot = syncSnapshot.start(execSvc);
                this.debuggerEngineImpl.setSyncSnapshot(syncSnapshot);
                syncSnapshotHolder.addSyncSnapshot(syncSnapshot, null);
                logger.info("GOT FIRST SYNC STATE");
                state.setDebuggerState(RunnerState.State.SYNC_STATE);
                if (isSkipSyncPoints) {
                    nextSync();
                }
            } catch (InterruptedException e) {
                logger.warning("got InterruptedException in startSync");
            }
        });
        startSyncThread.setName("startSyncThread");
        runningThreads.add(startSyncThread);
        startSyncThread.start();
        return createResolvedFuture("Started");
    }

    public FutureTask<String> nextSync() {
        if (this.state.getDebuggerState() == RunnerState.State.WAITING_FOR_EXTERNAL_EVENT)
            return createResolvedFuture("Waiting for external event");
        else if (this.state.getDebuggerState() == RunnerState.State.JS_DEBUG)
            return createResolvedFuture("Cant move next sync while in JS debug");
        else if (this.state.getDebuggerState() == RunnerState.State.RUNNING)
            return createResolvedFuture("BProg already in running state");
        else if (!isStarted())
            return createResolvedFuture("BProg not started yet");

        Thread nextSyncThread = new Thread(() -> {
            this.state.setDebuggerState(RunnerState.State.RUNNING);
            EventSelectionStrategy eventSelectionStrategy = this.bProg.getEventSelectionStrategy();
            Set<BEvent> possibleEvents = eventSelectionStrategy.selectableEvents(this.syncSnapshot);
            if (possibleEvents.isEmpty()) {
                if (this.bProg.isWaitForExternalEvents()) {
                    try {
                        this.state.setDebuggerState(RunnerState.State.WAITING_FOR_EXTERNAL_EVENT);
                        BEvent next = this.bProg.takeExternalEvent(); // and now we wait for external event
                        this.state.setDebuggerState(RunnerState.State.RUNNING);
                        if (next == null) {
                            return;
                        } else {
                            syncSnapshot.getExternalEvents().add(next);
                            possibleEvents = eventSelectionStrategy.selectableEvents(syncSnapshot);
                        }
                    } catch (Exception e) {
                        return;
                    }
                } else {
                    logger.info("Event queue empty, not need to wait to external event. terminating....");
                    printBPS();
                    onExit();
                    return;
                }
            }

            logger.info("Possible events(internal): " + possibleEvents);
            logger.info("External events: " + syncSnapshot.getExternalEvents());

            try {
                Optional<EventSelectionResult> eventOptional = eventSelectionStrategy.select(syncSnapshot, possibleEvents);
                if (eventOptional.isPresent()) {
                    EventSelectionResult esr = eventOptional.get();
                    BEvent event = esr.getEvent();
                    if (!esr.getIndicesToRemove().isEmpty()) {
                        removeExternalEvents(esr);
                    }
                    logger.info("Triggering event " + event);

                    syncSnapshot = syncSnapshot.triggerEvent(event, execSvc, new ArrayList<>());
                    this.debuggerEngineImpl.setSyncSnapshot(syncSnapshot);
                    state.setDebuggerState(RunnerState.State.SYNC_STATE);
                    logger.debug("Generate state from nextSync");
                    BPDebuggerState bpDebuggerState = debuggerEngineImpl.generateDebuggerState();
                    //send state via socket
                    syncSnapshotHolder.addSyncSnapshot(syncSnapshot, event);
                    logger.info("GOT NEW SYNC STATE");
                    if (isSkipSyncPoints)
                        nextSync();
                } else {
                    System.out.println("Events queue is empty");
                }
            } catch (InterruptedException e) {
                logger.warning("got InterruptedException in nextSync");
            }
        });
        nextSyncThread.setName("nextSyncThread");
        runningThreads.add(nextSyncThread);
        nextSyncThread.start();
        return createResolvedFuture("Executed next sync");
    }

    private void onExit() {
        try {
            System.out.println("process finished");
            onExitInterrupt.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printBPS() {
        System.out.println("BP THREADS COUNT: " + (syncSnapshot == null ? "null" : syncSnapshot.getBThreadSnapshots().size()));
    }

    private void removeExternalEvents(EventSelectionResult esr) {
        // the event selection affected the external event queue.
        List<BEvent> updatedExternals = new ArrayList<>(this.syncSnapshot.getExternalEvents());
        esr.getIndicesToRemove().stream().sorted(reverseOrder())
                .forEach(idxObj -> updatedExternals.remove(idxObj.intValue()));
        this.syncSnapshot = this.syncSnapshot.copyWith(updatedExternals);
    }

    public FutureTask<String> continueRun() {
        return !isSetup() ? createResolvedFuture("setup required") :
                debuggerEngineImpl.addCommand(new Continue());
    }

    public FutureTask<String> stepInto() {
        return !isSetup() ? createResolvedFuture("setup required") :
                debuggerEngineImpl.addCommand(new StepInto());
    }

    public FutureTask<String> stepOver() {
        return !isSetup() ? createResolvedFuture("setup required") :
                debuggerEngineImpl.addCommand(new StepOver());
    }

    public FutureTask<String> stepOut() {
        return !isSetup() ? createResolvedFuture("setup required") :
                debuggerEngineImpl.addCommand(new StepOut());
    }

    @Override
    public FutureTask<String> setBreakpoint(int lineNumber, boolean stopOnBreakpoint) {
        return !isSetup() ? createResolvedFuture("setup required") :
                resolveFuture(new SetBreakpoint(lineNumber, true).applyCommand(debuggerEngineImpl));
    }

    public FutureTask<String> getVars() {
        return !isSetup() ? createResolvedFuture("setup required") :
                debuggerEngineImpl.addCommand(new GetVars());
    }

    public FutureTask<String> exit() {
        return !isSetup() ? createResolvedFuture("setup required") :
                !isStarted() ? createResolvedFuture("The program has ended") :
                        debuggerEngineImpl.addCommand(new Exit());
    }

    @Override
    public FutureTask<String> stop() {
        if (!isSetup())
            return createResolvedFuture("setup required");
        while (!execSvc.isTerminated()) {
            runningThreads.forEach(Thread::interrupt);
            execSvc.shutdownNow();
            try {
                execSvc.awaitTermination(1500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!execSvc.isTerminated()) {
                logger.info("not yet terminated");
//                continueRun().run();
            }
        }
        return resolveFuture(new Stop().applyCommand(debuggerEngineImpl));
    }

    @Override
    public FutureTask<String> getState() {

        if (this.state.getDebuggerState() == RunnerState.State.JS_DEBUG)
            return createResolvedFuture("Cant get state in JS_DEBUG. updated state will be send when receiving");
        else if (this.state.getDebuggerState() == RunnerState.State.RUNNING)
            return createResolvedFuture("BProg already in running state. you will get the state when reach the next sync state");
        else if (!isStarted())
            return createResolvedFuture("BProg not started yet");
        logger.debug("Generate state from getState");
        BPDebuggerState bpDebuggerState = debuggerEngineImpl.generateDebuggerState();
        System.out.println(bpDebuggerState.toString());
        return resolveFuture(new GetState().applyCommand(debuggerEngineImpl));
    }

    @Override
    public FutureTask<String> toggleMuteBreakpoints(boolean toggleBreakPointStatus) {
        return !isSetup() ? createResolvedFuture("setup required")
                : createResolvedFuture(debuggerEngineImpl.toggleMuteBreakpoints(toggleBreakPointStatus));
    }

    public FutureTask<String> addExternalEvent(String externalEvent) {
        this.bProg.enqueueExternalEvent(new BEvent(externalEvent));
        return createResolvedFuture("Added external event: " + externalEvent);
    }

    @Override
    public FutureTask<String> removeExternalEvent(String externalEvent) {
        List<BEvent> updatedExternals = new ArrayList<>(this.syncSnapshot.getExternalEvents());
        int indexToRemove = -1;

        for (int i = 0; i < updatedExternals.size(); i++) {
            if (updatedExternals.get(i).getName().equals(externalEvent)) {
                indexToRemove = i;
            }
        }
        if (indexToRemove >= 0) {
            updatedExternals.remove(indexToRemove);
        }

        syncSnapshot = syncSnapshot.copyWith(updatedExternals);
        return createResolvedFuture("Removed external event: " + externalEvent);
    }

    @Override
    public FutureTask<String> setWaitForExternalEvents(boolean shouldWait) {
        this.bProg.setWaitForExternalEvents(shouldWait);
        return createResolvedFuture("Wait For External events is set for:" + shouldWait);
    }

    private FutureTask<String> createResolvedFuture(String result) {
        FutureTask<String> futureTask = new FutureTask<>(() -> result);
        futureTask.run();
        return futureTask;
    }

    private FutureTask<String> resolveFuture(FutureTask<String> futureTask) {
        futureTask.run();
        return futureTask;
    }

    //OLD METHOD TO RUN BPROG - JUST FOR REFERENCE
    @Override
    public void start(Map<Integer, Boolean> breakpoints) {
        if (!isSetup) {
            setup(breakpoints, false);
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

}
