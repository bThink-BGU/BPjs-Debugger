package il.ac.bgu.se.bp.execution;

import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.*;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionResult;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionStrategy;
import il.ac.bgu.se.bp.debugger.BPJsDebugger;
import il.ac.bgu.se.bp.debugger.commands.*;
import il.ac.bgu.se.bp.debugger.engine.DebuggerEngine;
import il.ac.bgu.se.bp.debugger.engine.DebuggerEngineImpl;
import il.ac.bgu.se.bp.debugger.engine.SyncSnapshotHolder;
import il.ac.bgu.se.bp.debugger.engine.SyncSnapshotHolderImpl;
import il.ac.bgu.se.bp.debugger.engine.events.BPExitEvent;
import il.ac.bgu.se.bp.socket.state.BPDebuggerState;
import il.ac.bgu.se.bp.socket.state.EventInfo;
import il.ac.bgu.se.bp.error.ErrorCode;
import il.ac.bgu.se.bp.logger.Logger;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;
import il.ac.bgu.se.bp.rest.response.GetSyncSnapshotsResponse;
import il.ac.bgu.se.bp.utils.DebuggerPrintStream;
import il.ac.bgu.se.bp.utils.DebuggerStateHelper;
import il.ac.bgu.se.bp.utils.Pair;
import il.ac.bgu.se.bp.utils.observer.BPEvent;
import il.ac.bgu.se.bp.utils.observer.Publisher;
import il.ac.bgu.se.bp.utils.observer.Subscriber;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static il.ac.bgu.se.bp.utils.ResponseHelper.createErrorResponse;
import static il.ac.bgu.se.bp.utils.ResponseHelper.createSuccessResponse;
import static java.util.Collections.reverseOrder;

/**
 * Runs a {@link BProgram} in debug mode.
 */
public class BPJsDebuggerImpl implements BPJsDebugger<BooleanResponse>, Publisher<BPEvent> {
    private final static AtomicInteger debuggerThreadIdGenerator = new AtomicInteger(0);

    private ExecutorService execSvc;
    private Logger logger;
    private BProgram bprog;
    private DebuggerEngine<BProgramSyncSnapshot> debuggerEngine;
    private BProgramSyncSnapshot syncSnapshot = null;
    private volatile boolean isBProgSetup = false; //indicated if bprog after setup
    private volatile boolean isSetup = false;
    private volatile boolean isStarted = false;
    private volatile boolean isSkipSyncPoints = false;
    private RunnerState state = new RunnerState();
    private List<Thread> runningThreads;
    private final SyncSnapshotHolder<BProgramSyncSnapshot, BEvent> syncSnapshotHolder = new SyncSnapshotHolderImpl();
    private DebuggerStateHelper debuggerStateHelper = new DebuggerStateHelper();
    private final List<BProgramRunnerListener> listeners = new ArrayList<>();
    private List<Subscriber<BPEvent>> subscribers;
    private final String debuggerId;
    private final DebuggerPrintStream debuggerPrintStream = new DebuggerPrintStream();

    public BPJsDebuggerImpl(String debuggerId, String filename) {
        runningThreads = new LinkedList<>();
        this.debuggerId = debuggerId;
        initDebugger(debuggerId, filename);
    }

    private void initDebugger(String debuggerId, String filename) {
        String debuggerThreadId = "BPJsDebuggerRunner-" + debuggerThreadIdGenerator.incrementAndGet();
        execSvc = ExecutorServiceMaker.makeWithName(debuggerThreadId);
        logger = new Logger(BPJsDebuggerImpl.class, debuggerThreadId);
        subscribers = new ArrayList<>();
        debuggerEngine = new DebuggerEngineImpl(debuggerId, filename, state, debuggerStateHelper);
        debuggerPrintStream.setDebuggerId(debuggerId);
        listeners.add(new PrintBProgramRunnerListener(debuggerPrintStream));
        bprog = new ResourceBProgram(filename);

        bprog.setAddBThreadCallback((bp, bt) -> listeners.forEach(l -> l.bthreadAdded(bp, bt)));
    }

    @Override
    public BooleanResponse setup(Map<Integer, Boolean> breakpoints, boolean isSkipBreakpoints, boolean isSkipSyncPoints) {
        if (!isBProgSetup) { // may get twice to setup - must do bprog setup first time only
            listeners.forEach(l -> l.starting(bprog));
            syncSnapshot = bprog.setup();
            bprog.setLoggerOutputStreamer(debuggerPrintStream);
            syncSnapshot.getBThreadSnapshots().forEach(sn->listeners.forEach( l -> l.bthreadAdded(bprog, sn)) );
            isBProgSetup = true;
            if (syncSnapshot.getFailedAssertion() != null) {
                return createErrorResponse(ErrorCode.BP_SETUP_FAIL);// todo: add failed assertion message
            }
        }
        setIsSkipSyncPoints(isSkipSyncPoints);
        try {
            debuggerEngine.setupBreakpoints(breakpoints);
            debuggerEngine.toggleMuteBreakpoints(isSkipBreakpoints);
        }
        catch (IllegalArgumentException e){
            logger.error("cant set breakpoint line {0} to true ", e.getMessage());
            return createErrorResponse(ErrorCode.BREAKPOINT_NOT_ALLOWED);
        }
        debuggerEngine.setSyncSnapshot(syncSnapshot);
        setIsSetup(true);
        state.setDebuggerState(RunnerState.State.STOPPED);

//        this.bProg.setWaitForExternalEvents(true);        //todo: add wait for external event toggle
        return createSuccessResponse();
    }

    @Override
    public synchronized BooleanResponse setIsSkipSyncPoints(boolean isSkipSyncPoints) {
        this.isSkipSyncPoints = isSkipSyncPoints;
        return createSuccessResponse();
    }

    @Override
    public GetSyncSnapshotsResponse getSyncSnapshotsHistory() {
        SortedMap<Long, BPDebuggerState> syncSnapshotsHistory = new TreeMap<>();

        syncSnapshotHolder.getAllSyncSnapshots().forEach((time, bProgramSyncSnapshotBEventPair) -> {
            BPDebuggerState bpDebuggerState = debuggerStateHelper.generateDebuggerState(bProgramSyncSnapshotBEventPair.getLeft(), state, null);
            BEvent chosenEvent = bProgramSyncSnapshotBEventPair.getRight();
            bpDebuggerState.setChosenEvent(new EventInfo(chosenEvent == null ? null : chosenEvent.getName()));
            syncSnapshotsHistory.put(time, bpDebuggerState);
        });

        return new GetSyncSnapshotsResponse(syncSnapshotsHistory);
    }

    @Override
    public BooleanResponse setSyncSnapshots(long snapShotTime) {
        BProgramSyncSnapshot newSnapshot = syncSnapshotHolder.popKey(snapShotTime);
        if (newSnapshot == null) {
            return createErrorResponse(ErrorCode.CANNOT_REPLACE_SNAPSHOT);
        }

        syncSnapshot = newSnapshot;
        nextSync();
        return createSuccessResponse();
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

    private void updateRecentlyRegisteredBT(){
        Set<BThreadSyncSnapshot> recentlyRegisteredBthreads = bprog.getRecentlyRegisteredBthreads();
        Set<Pair<String, Object>> recentlyRegistered = new HashSet<>();
        for(BThreadSyncSnapshot b: recentlyRegisteredBthreads){
            recentlyRegistered.add(new Pair<>(b.getName(), b.getEntryPoint()));
        }
        debuggerStateHelper.setRecentlyRegisteredBthreads(recentlyRegistered);
    }

    @Override
    public BooleanResponse startSync(boolean isSkipBreakpoints, boolean isSkipSyncPoints) {
        if (!isSetup()) {
            setup(new HashMap<>(), isSkipSyncPoints, isSkipBreakpoints);
        }
        setItStarted(true);
        Thread startSyncThread = new Thread(() -> {
            try {
                updateRecentlyRegisteredBT();
                listeners.forEach(l -> l.started(bprog));
                syncSnapshot = syncSnapshot.start(execSvc);
                if ( ! syncSnapshot.isStateValid() ) {
                    FailedAssertion failedAssertion = syncSnapshot.getFailedAssertion();
                    listeners.forEach( l->l.assertionFailed(bprog, failedAssertion));
                    state.setDebuggerState(RunnerState.State.STOPPED);
                    logger.error("Start sync fatal error");
                    return;
                }
                state.setDebuggerState(RunnerState.State.SYNC_STATE);
                debuggerEngine.setSyncSnapshot(syncSnapshot);
                syncSnapshotHolder.addSyncSnapshot(syncSnapshot, null);
                logger.debug("Generate state from startSync");
                debuggerEngine.onStateChanged();
                logger.info("~FIRST SYNC STATE~");
                state.setDebuggerState(RunnerState.State.SYNC_STATE);
                if (isSkipSyncPoints) {
                    nextSync();
                }
            } catch (InterruptedException e) {
                logger.warning("got InterruptedException in startSync");
            }
            catch (RejectedExecutionException e){
                logger.error("Forced to stop");
            }
        });
        startSyncThread.setName("startSyncThread");
        runningThreads.add(startSyncThread);
        startSyncThread.start();
        return createSuccessResponse();
    }

    @Override
    public BooleanResponse nextSync() {
        if (this.state.getDebuggerState() == RunnerState.State.WAITING_FOR_EXTERNAL_EVENT)
            return createErrorResponse(ErrorCode.WAITING_FOR_EXTERNAL_EVENT);
        else if (this.state.getDebuggerState() == RunnerState.State.JS_DEBUG)
            return createErrorResponse(ErrorCode.NOT_IN_BP_SYNC_STATE);
        else if (this.state.getDebuggerState() == RunnerState.State.RUNNING)
            return createErrorResponse(ErrorCode.ALREADY_RUNNING);
        else if (!isStarted())
            return createErrorResponse(ErrorCode.NOT_STARTED);

        Thread nextSyncThread = createNextSyncThread();
        nextSyncThread.setName("nextSyncThread");
        runningThreads.add(nextSyncThread);
        nextSyncThread.start();
        return createSuccessResponse();
    }

    private Thread createNextSyncThread() {
        return new Thread(() -> {
            state.setDebuggerState(RunnerState.State.RUNNING);
            EventSelectionStrategy eventSelectionStrategy = this.bprog.getEventSelectionStrategy();
            Set<BEvent> possibleEvents = eventSelectionStrategy.selectableEvents(this.syncSnapshot);
            if (possibleEvents.isEmpty()) {
                possibleEvents = nextSyncOnNoPossibleEvents(eventSelectionStrategy, possibleEvents);
            }

            logger.info("Possible events(internal): " + possibleEvents);
            logger.info("External events: " + syncSnapshot.getExternalEvents());

            try {
                Optional<EventSelectionResult> eventOptional = eventSelectionStrategy.select(syncSnapshot, possibleEvents);
                if (eventOptional.isPresent()) {
                    nextSyncOnChosenEvent(eventOptional.get());
                }
                else {
                    logger.info("Events queue is empty");
                }
            } catch (InterruptedException e) {
                logger.warning("got InterruptedException in nextSync");
            }
        });
    }

    private void nextSyncOnChosenEvent(EventSelectionResult eventSelectionResult) throws InterruptedException {
        BEvent event = eventSelectionResult.getEvent();
        if (!eventSelectionResult.getIndicesToRemove().isEmpty()) {
            removeExternalEvents(eventSelectionResult);
        }
        logger.info("Triggering event " + event);
        updateRecentlyRegisteredBT();
        syncSnapshot = syncSnapshot.triggerEvent(event, execSvc, listeners);
        if ( ! syncSnapshot.isStateValid() ) {
            FailedAssertion failedAssertion = syncSnapshot.getFailedAssertion();
            listeners.forEach( l->l.assertionFailed(bprog, failedAssertion));
            state.setDebuggerState(RunnerState.State.STOPPED);
            logger.error("Next Sync fatal error");
            return;
        }
        state.setDebuggerState(RunnerState.State.SYNC_STATE);
        debuggerEngine.setSyncSnapshot(syncSnapshot);
        debuggerStateHelper.setLastChosenEvent(event);
        logger.debug("Generate state from nextSync");
        debuggerEngine.onStateChanged();
        syncSnapshotHolder.addSyncSnapshot(syncSnapshot, event);
        logger.info("~NEW SYNC STATE~");
        if (isSkipSyncPoints)
            nextSync();
    }

    private Set<BEvent> nextSyncOnNoPossibleEvents(EventSelectionStrategy eventSelectionStrategy, Set<BEvent> possibleEvents) {
        if (bprog.isWaitForExternalEvents()) {
            try {
                state.setDebuggerState(RunnerState.State.WAITING_FOR_EXTERNAL_EVENT);
                listeners.forEach( l->l.superstepDone(bprog) );
                BEvent next = bprog.takeExternalEvent(); // and now we wait for external event
                state.setDebuggerState(RunnerState.State.RUNNING);
                if (next == null) {
                    return possibleEvents;
                }
                else {
                    syncSnapshot.getExternalEvents().add(next);
                    return eventSelectionStrategy.selectableEvents(syncSnapshot);
                }
            } catch (Exception e) {
                return possibleEvents;
            }
        }
        else {
            logger.info("Event queue empty, not need to wait to external event. terminating....");
            listeners.forEach(l->l.ended(bprog));
            onExit();
            return possibleEvents;
        }
    }

    private void onExit() {
        notifySubscribers(new BPExitEvent(debuggerId));
    }

    private void removeExternalEvents(EventSelectionResult esr) {
        // the event selection affected the external event queue.
        List<BEvent> updatedExternals = new ArrayList<>(syncSnapshot.getExternalEvents());
        esr.getIndicesToRemove().stream().sorted(reverseOrder())
                .forEach(idxObj -> updatedExternals.remove(idxObj.intValue()));
        this.syncSnapshot = this.syncSnapshot.copyWith(updatedExternals);
    }

    @Override
    public BooleanResponse continueRun() {
        if (!isSetup())
            return createErrorResponse(ErrorCode.SETUP_REQUIRED);
        if (this.state.getDebuggerState() != RunnerState.State.JS_DEBUG) {
            return createErrorResponse(ErrorCode.NOT_IN_JS_DEBUG_STATE);
        }
        return addCommandIfStarted(new Continue());
    }

    @Override
    public BooleanResponse stepInto() {
        if (!isSetup())
            return createErrorResponse(ErrorCode.SETUP_REQUIRED);
        if (this.state.getDebuggerState() != RunnerState.State.JS_DEBUG) {
            return createErrorResponse(ErrorCode.NOT_IN_JS_DEBUG_STATE);
        }
        return addCommandIfStarted(new StepInto());
    }

    @Override
    public BooleanResponse stepOver() {
        if (!isSetup())
            return createErrorResponse(ErrorCode.SETUP_REQUIRED);
        if (this.state.getDebuggerState() != RunnerState.State.JS_DEBUG) {
            return createErrorResponse(ErrorCode.NOT_IN_JS_DEBUG_STATE);
        }
        return addCommandIfStarted(new StepOver());
    }

    @Override
    public BooleanResponse stepOut() {
        if (!isSetup())
            return createErrorResponse(ErrorCode.SETUP_REQUIRED);
        if (this.state.getDebuggerState() != RunnerState.State.JS_DEBUG) {
            return createErrorResponse(ErrorCode.NOT_IN_JS_DEBUG_STATE);
        }
        return addCommandIfStarted(new StepOut());
    }

    @Override
    public BooleanResponse setBreakpoint(final int lineNumber, final boolean stopOnBreakpoint) {
        if (!isSetup()) {
            return createErrorResponse(ErrorCode.SETUP_REQUIRED);
        }
        return new SetBreakpoint(lineNumber, stopOnBreakpoint).applyCommand(debuggerEngine);
    }

    @Override
    public BooleanResponse stop() {
        if (!isSetup())
            return createErrorResponse(ErrorCode.SETUP_REQUIRED);
        //todo: remove?
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
            }
        }
        setItStarted(false);
        return new Stop().applyCommand(debuggerEngine);
    }

    @Override
    public BooleanResponse getState() {
        if (!isSetup())
            return createErrorResponse(ErrorCode.SETUP_REQUIRED);
        else if (state.getDebuggerState() == RunnerState.State.JS_DEBUG)
            return createErrorResponse(ErrorCode.NOT_IN_BP_SYNC_STATE);
        else if (state.getDebuggerState() == RunnerState.State.RUNNING)
            return createErrorResponse(ErrorCode.ALREADY_RUNNING);
        return new GetState().applyCommand(debuggerEngine);
    }

    @Override
    public BooleanResponse toggleMuteBreakpoints(boolean toggleBreakPointStatus) {
        if (!isSetup())
            return new BooleanResponse(false, ErrorCode.SETUP_REQUIRED);
        return new ToggleMuteBreakpoints(toggleBreakPointStatus).applyCommand(debuggerEngine);
    }

    @Override
    public BooleanResponse addExternalEvent(String externalEvent) {
        if (StringUtils.isEmpty(externalEvent)) {
            return createErrorResponse(ErrorCode.INVALID_EVENT);
        }
        bprog.enqueueExternalEvent(new BEvent(externalEvent));
        return createSuccessResponse();
    }

    @Override
    public BooleanResponse removeExternalEvent(String externalEvent) {
        if (StringUtils.isEmpty(externalEvent)) {
            return createErrorResponse(ErrorCode.INVALID_EVENT);
        }
        List<BEvent> updatedExternals = new ArrayList<>(syncSnapshot.getExternalEvents());
        updatedExternals.removeIf(bEvent -> bEvent.getName().equals(externalEvent));
        syncSnapshot = syncSnapshot.copyWith(updatedExternals);
        return createSuccessResponse();
    }

    @Override
    public BooleanResponse setWaitForExternalEvents(boolean shouldWait) {
        bprog.setWaitForExternalEvents(shouldWait);
        return createSuccessResponse();
    }

    private BooleanResponse addCommandIfStarted(DebuggerCommand debuggerCommand) {
        if (!isSetup()) {
            return createErrorResponse(ErrorCode.SETUP_REQUIRED);
        }
        try {
            debuggerEngine.addCommand(debuggerCommand);
            return createSuccessResponse();
        } catch (Exception e) {
            logger.error("failed adding command", e);
            e.printStackTrace();
        }
        return createErrorResponse(ErrorCode.FAILED_ADDING_COMMAND);
    }

    @Override
    public void subscribe(Subscriber<BPEvent> subscriber) {
        subscribers.add(subscriber);
        debuggerEngine.subscribe(subscriber);
        debuggerPrintStream.subscribe(subscriber);
    }

    @Override
    public void unsubscribe(Subscriber<BPEvent> subscriber) {
        subscribers.remove(subscriber);
        debuggerEngine.unsubscribe(subscriber);
        debuggerPrintStream.unsubscribe(subscriber);
    }

    @Override
    public void notifySubscribers(BPEvent event) {
        for (Subscriber<BPEvent> subscriber : subscribers) {
            subscriber.update(event);
        }
    }

//    //OLD METHOD TO RUN BPROG - JUST FOR REFERENCE
//    public void start(Map<Integer, Boolean> breakpoints) {
//        if (!isSetup) {
//            setup(breakpoints, false);
//            return;
//        }
//        BProgramRunner rnr = new BProgramRunner();
//        rnr.addListener(new PrintBProgramRunnerListener());
//        rnr.addListener(new BProgramRunnerListenerAdapter() {
//            @Override
//            public void ended(BProgram bp) {
//                setItStarted(false);
//            }
//        });
//        rnr.setBProgram(bProg);
//        setItStarted(true);
//        new Thread(rnr).start();
//    }
}