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
        setIsSetup(true);
//        this.bProg.setWaitForExternalEvents(true);        //todo: add wait for external event toggle
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
                syncSnapshotHolder.addSyncSnapshot(syncSnapshot, null);
                System.out.println("GOT NEW SYNC STATE - First sync state");
                state.setDebuggerState(RunnerState.State.SYNC_STATE);
                if (isSkipSyncPoints) {
                    nextSync();
                }
            } catch (InterruptedException e) {
                logger.warning("got InterruptedException in startSync");
            }
        });
        runningThreads.add(startSyncThread);
        startSyncThread.start();
        return createResolvedFuture("Started");
    }

    public FutureTask<String> nextSync() {
        if(this.state.getDebuggerState() == RunnerState.State.WAITING_FOR_EXTERNAL_EVENT)
            return createResolvedFuture("Waiting for external event");
        else if(this.state.getDebuggerState() == RunnerState.State.JS_DEBUG)
            return createResolvedFuture("Cant move next sync while in JS debug");
        else if(this.state.getDebuggerState() == RunnerState.State.RUNNING)
            return createResolvedFuture("bprog already in running state");
        else if(!isStarted())
            return createResolvedFuture("bprog not started yet");

        Thread nextSyncThread = new Thread(() -> {
            this.state.setDebuggerState(RunnerState.State.RUNNING);
            EventSelectionStrategy eventSelectionStrategy = this.bProg.getEventSelectionStrategy();
            Set<BEvent> possibleEvents = eventSelectionStrategy.selectableEvents(this.syncSnapshot);
            if (possibleEvents.isEmpty()) {
                if (this.bProg.isWaitForExternalEvents()) {
                    try{
                        this.state.setDebuggerState(RunnerState.State.WAITING_FOR_EXTERNAL_EVENT);
                        BEvent next = this.bProg.takeExternalEvent(); // and now we wait for external event
                        this.state.setDebuggerState(RunnerState.State.RUNNING);
                        if (next == null) {
                            return;
                        } else {
                            syncSnapshot.getExternalEvents().add(next);
                            possibleEvents = eventSelectionStrategy.selectableEvents(syncSnapshot);
                        }
                    }
                    catch (Exception e){
                        return;
                    }
                } else {
                    System.out.println("Not events wait for external events. termination?");
                    printBPS();
                    onExit();
                    return;
                }
            }

            System.out.println("all events: " + possibleEvents);
            System.out.println("External events: " + syncSnapshot.getExternalEvents());
            try {
                Optional<EventSelectionResult> eventOptional = eventSelectionStrategy.select(syncSnapshot, possibleEvents);
                if(eventOptional.isPresent())
                {
                    EventSelectionResult esr = eventOptional.get();
                    BEvent event = esr.getEvent();
                    System.out.println("Selected event: "+ event);

                    if ( ! esr.getIndicesToRemove().isEmpty() ) {
                        removeExternalEvents(esr);
                    }
                    syncSnapshot = syncSnapshot.triggerEvent(event, execSvc, new ArrayList<>());
                    syncSnapshotHolder.addSyncSnapshot(syncSnapshot, event);
                    state.setDebuggerState(RunnerState.State.SYNC_STATE);
                    System.out.println("GOT NEW SYNC STATE");
                    if (isSkipSyncPoints)
                        nextSync();
                }
                else {
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
        System.out.println("BPSYNC COUNT: " + (syncSnapshot == null ? "null" : syncSnapshot.getBThreadSnapshots().size()));
    }

    private void removeExternalEvents(EventSelectionResult esr) {
        // the event selection affected the external event queue.
        List<BEvent> updatedExternals = new ArrayList<>(this.syncSnapshot.getExternalEvents());
        esr.getIndicesToRemove().stream().sorted(reverseOrder())
                .forEach( idxObj -> updatedExternals.remove(idxObj.intValue()) );
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
        return  !isSetup() ? createResolvedFuture("setup required") :
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
    public FutureTask<String> getState(){

        Set<BThreadSyncSnapshot> bThreadSyncSnapshot = syncSnapshot.getBThreadSnapshots();
        for(BThreadSyncSnapshot b: bThreadSyncSnapshot) {

            System.out.println(b.getName());
//            Map<Object, Object> s = b.getContinuationProgramState().getVisibleVariables();
//            for (Map.Entry e : s.entrySet()) {
//                System.out.println(e.getKey().toString());
//                System.out.println(e.getValue().toString());
//
//            }

            ScriptableObject scope = (ScriptableObject) b.getScope();
            try {

                Object implementation = getValue(scope, "implementation");
                Dim.StackFrame debuggerFrame = (Dim.StackFrame) getValue(implementation, "debuggerFrame");
                Map<Integer, Map<String, String>> env = debuggerEngineImpl.getEnv(debuggerFrame.contextData(), implementation);
                for (Map.Entry e : env.entrySet()) {
                    System.out.println(e.getKey() + ":" + e.getValue());
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            EventSet wait = b.getSyncStatement().getWaitFor();
            EventSet blocked =b.getSyncStatement().getBlock();
            List<BEvent> requested = b.getSyncStatement().getRequest().stream().collect(Collectors.toList());
            System.out.println(wait);
            System.out.println(blocked);
            System.out.println(requested);
//
//            Map<Object, Object> globalVariables = new HashMap<>();
//            NativeContinuation nc = ((NativeContinuation)b.getContinuation());
//            Map<Object, Object> variables = b.getContinuationProgramState().getVisibleVariables();
//            System.out.println(variables.toString());
//            getGlobalValues(nc, variables, globalVariables);
//            Map<Object, Object> localVariables = new HashMap<>();
//            for(Object var: variables.keySet())
//                if(!globalVariables.containsKey(var))
//                    localVariables.put(var, variables.get(var));
//            for (Map.Entry e : globalVariables.entrySet()) {
//                System.out.println(e);
//            }
//            for (Map.Entry e : localVariables.entrySet()) {
//                System.out.println(e);
//            }
//
//            System.out.println();
//        }
        }
        return resolveFuture(new GetState().applyCommand(debuggerEngineImpl));

    }



    private Object getValue(Object instance, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field fld = instance.getClass().getDeclaredField(fieldName);
        fld.setAccessible(true);
        return fld.get(instance);
    }
    private void getGlobalValues(NativeContinuation nc,  Map<Object, Object> variables, Map<Object, Object> globalVariables) {
        ScriptableObject current = nc;
        ScriptableObject currentScope = nc;
        try {
            Context.enter();
            while (current != null) {
                for(Object o: current.getIds())
                    addVar(current, globalVariables, o, variables.get(o));
                if (current.getPrototype() != null)
                    current = (ScriptableObject) current.getPrototype();
                else { // go to the next
                    current = (ScriptableObject) currentScope.getParentScope();
                    currentScope = current;
                }
            }
        } finally {
            Context.exit();
        }
    }

    private void addVar(ScriptableObject current, Map<Object, Object> variables, Object var, Object val) {
        if (!variables.containsKey(var) && !var.equals("bp")) {
            Object variableContent = current.get(var);
            if (variableContent instanceof Undefined) return;
            variables.put(var, val);
        }
    }
    @Override
    public FutureTask<String> toggleMuteBreakpoints(boolean toggleBreakPointStatus) {
        return !isSetup() ? createResolvedFuture("setup required")
                : createResolvedFuture(debuggerEngineImpl.toggleMuteBreakpoints(toggleBreakPointStatus));
    }

    public FutureTask<String> addExternalEvent(String externalEvent){
        this.bProg.enqueueExternalEvent(new BEvent(externalEvent));

        return createResolvedFuture("Added external event: "+ externalEvent );
    }

    @Override
    public FutureTask<String> removeExternalEvent(String externalEvent) {
        List<BEvent> updatedExternals = new ArrayList<>(this.syncSnapshot.getExternalEvents());
        int indexToRemove = -1;

        for(int i =0; i<updatedExternals.size(); i++) {
            if(updatedExternals.get(i).getName().equals(externalEvent)) {
                indexToRemove = i;
            }
        }
        if(indexToRemove >= 0){
            updatedExternals.remove(indexToRemove);
        }

        syncSnapshot = syncSnapshot.copyWith(updatedExternals);
        return createResolvedFuture("Removed external event: "+ externalEvent );
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
}
