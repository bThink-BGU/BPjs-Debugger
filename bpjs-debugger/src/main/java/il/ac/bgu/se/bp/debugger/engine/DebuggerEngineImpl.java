package il.ac.bgu.se.bp.debugger.engine;

import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.se.bp.debugger.commands.DebuggerCommand;
import il.ac.bgu.se.bp.debugger.engine.dim.DimHelper;
import il.ac.bgu.se.bp.debugger.engine.dim.DimHelperImpl;
import il.ac.bgu.se.bp.debugger.engine.events.BPStateEvent;
import il.ac.bgu.se.bp.socket.state.BPDebuggerState;
import il.ac.bgu.se.bp.debugger.RunnerState;
import il.ac.bgu.se.bp.logger.Logger;
import il.ac.bgu.se.bp.utils.DebuggerStateHelper;
import il.ac.bgu.se.bp.utils.observer.BPEvent;
import il.ac.bgu.se.bp.utils.observer.Publisher;
import il.ac.bgu.se.bp.utils.observer.BPEventPublisherImpl;
import il.ac.bgu.se.bp.utils.observer.Subscriber;
import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.debugger.Dim;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class DebuggerEngineImpl implements DebuggerEngine<BProgramSyncSnapshot> {
    private Logger logger;
    private DimHelper dimHelper;
    private final BlockingQueue<DebuggerCommand> queue;
    private final String filename;
    private Dim.ContextData lastContextData = null;
    private volatile boolean isRunning;
    private final RunnerState state;
    private volatile boolean areBreakpointsMuted = false;
    private BProgramSyncSnapshot syncSnapshot = null;
    private DebuggerStateHelper debuggerStateHelper;
    private Publisher<BPEvent> publisher;
    private String debuggerId;

    public DebuggerEngineImpl(String debuggerId, String filename, RunnerState state, DebuggerStateHelper debuggerStateHelper) {
        this.filename = filename;
        this.state = state;
        this.debuggerStateHelper = debuggerStateHelper;
        this.debuggerId = debuggerId;
        this.logger = new Logger(DebuggerEngineImpl.class, debuggerId);
        publisher = new BPEventPublisherImpl();
        queue = new ArrayBlockingQueue<>(1);
        initDim();
        setIsRunning(true);
    }

    private void initDim() {
        dimHelper = new DimHelperImpl();
        dimHelper.setGuiCallback(this);
        dimHelper.attachTo(ContextFactory.getGlobal());
    }

    public void setupBreakpoints(Map<Integer, Boolean> breakpoints) throws IllegalArgumentException {
        if (breakpoints == null) {
            return;
        }
        breakpoints.forEach(this::setBreakpoint);
    }

    @Override
    public void updateSourceText(Dim.SourceInfo sourceInfo) {
    }

    @Override
    public void enterInterrupt(Dim.StackFrame stackFrame, String s, String s1) {
        System.out.println("Breakpoint reached- " + s + " Line no: " + stackFrame.getLineNumber());
        state.setDebuggerState(RunnerState.State.JS_DEBUG);
        lastContextData = stackFrame.contextData();

        logger.debug("Get state from enterInterrupt");
        if (areBreakpointsMuted) {
            continueRun();
        }
        else {
            onStateChanged();
        }
    }

    @Override
    public boolean isGuiEventThread() {
        return true;
    }

    @Override
    public void dispatchNextGuiEvent() {
        try {
            if (!debuggerStateHelper.getLastState().equals(debuggerStateHelper.peekNextState(syncSnapshot, state, lastContextData))) {
                logger.info("Getting state from dispatchNextGuiEvent");
                onStateChanged();
            }
            if (isRunning()) {
                DebuggerCommand debuggerCommand = queue.take();
                logger.info("applying command " + debuggerCommand.toString());
                debuggerCommand.applyCommand(this);
            }
            else {
                logger.error("IM HERE!");
            }
        } catch (Exception e) {
            logger.error("failed on dispatchNextGuiEvent", e);
        }
    }

    @Override
    public void addCommand(DebuggerCommand command) throws Exception {
        queue.add(command);
    }

    private synchronized boolean isRunning() {
        return isRunning;
    }

    private synchronized void setIsRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    @Override
    public synchronized void toggleMuteBreakpoints(boolean toggleBreakPointStatus) {
        this.areBreakpointsMuted = toggleBreakPointStatus;
    }

    @Override
    public void stop() {
        dimHelper.stop();
        setIsRunning(false);
    }

    @Override
    public void stepOut() {
        dimHelper.setReturnValue(Dim.STEP_OUT);
    }

    @Override
    public void stepInto() {
        dimHelper.setReturnValue(Dim.STEP_INTO);
//        dim.setBreakOnEnter(true); //possible bug because BP
    }

    @Override
    public void stepOver() {
        dimHelper.setReturnValue(Dim.STEP_OVER);
    }

    @Override
    public void continueRun() {
        dimHelper.go();
    }

    @Override
    public boolean isBreakpointAllowed(int lineNumber) {
        return dimHelper.isBreakpointAllowed(lineNumber, filename);
    }

    @Override
    public void setBreakpoint(int lineNumber, boolean stopOnBreakpoint) throws IllegalArgumentException {
        dimHelper.setBreakpoint(lineNumber, stopOnBreakpoint, filename);
    }

    @Override
    public void setSyncSnapshot(BProgramSyncSnapshot syncSnapshot) {
        this.syncSnapshot = syncSnapshot;
    }


    @Override
    public void onStateChanged() {
        BPDebuggerState newState = debuggerStateHelper.generateDebuggerState(syncSnapshot, state, lastContextData);
        notifySubscribers(new BPStateEvent(debuggerId, newState));
    }

    @Override
    public void getState() {
        onStateChanged();
    }

    @Override
    public void subscribe(Subscriber subscriber) {
        publisher.subscribe(subscriber);
    }

    @Override
    public void unsubscribe(Subscriber subscriber) {
        publisher.unsubscribe(subscriber);
    }

    @Override
    public void notifySubscribers(BPEvent event) {
        publisher.notifySubscribers(event);
    }
}