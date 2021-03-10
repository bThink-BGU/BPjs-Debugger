package il.ac.bgu.se.bp.debugger.engine;

import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.se.bp.debugger.commands.DebuggerCommand;
import il.ac.bgu.se.bp.debugger.state.BPDebuggerState;
import il.ac.bgu.se.bp.execution.RunnerState;
import il.ac.bgu.se.bp.logger.Logger;
import il.ac.bgu.se.bp.utils.DebuggerStateHelper;
import il.ac.bgu.se.bp.utils.Pair;
import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.debugger.Dim;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DebuggerEngineImpl implements DebuggerEngine<BProgramSyncSnapshot> {
    private final Logger logger = new Logger(DebuggerEngineImpl.class);
    private Dim dim;
    private final BlockingQueue<DebuggerCommand> queue;
    private final String filename;
    private Dim.ContextData lastContextData = null;
    private volatile boolean isRunning;
    private final RunnerState state;
    private volatile boolean areBreakpointsMuted = false;
    private BProgramSyncSnapshot syncSnapshot = null;
    private final Function<BPDebuggerState, Void> onStateChangedEvent;
    private DebuggerStateHelper debuggerStateHelper;

    public DebuggerEngineImpl(String filename, RunnerState state, Function<BPDebuggerState, Void> onStateChangedEvent, DebuggerStateHelper debuggerStateHelper) {
        this.onStateChangedEvent = onStateChangedEvent;
        this.filename = filename;
        this.state = state;
        this.debuggerStateHelper = debuggerStateHelper;
        queue = new ArrayBlockingQueue<>(1);
        dim = new Dim();
        dim.setGuiCallback(this);
        dim.attachTo(ContextFactory.getGlobal());
        setIsRunning(true);
    }

    public void setupBreakpoints(Map<Integer, Boolean> breakpoints) {
        if (breakpoints == null)
            return;
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
        } else {
            onStateChanged();
        }
    }

    @Override
    public boolean isGuiEventThread() {
        return true;
    }

    @Override
    public void dispatchNextGuiEvent() throws InterruptedException {
        try {
            logger.info("Getting state from dispatchNextGuiEvent");
            //onStateChanged(); todo
            queue.take().applyCommand(this);
        } catch (Exception e) {
            logger.error("failed on dispatchNextGuiEvent", e);
        }
    }

    @Override
    public void addCommand(DebuggerCommand command) {
        queue.add(command);
    }

    private synchronized boolean isRunning() {
        return isRunning;
    }

    private synchronized void setIsRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    private synchronized void setAreBreakpointsMuted(boolean areBreakpointsMuted) {
        this.areBreakpointsMuted = areBreakpointsMuted;
    }

    @Override
    public void stop() {
        dim.setReturnValue(Dim.EXIT);
        dim = null;
        setIsRunning(false);
    }

    @Override
    public void toggleMuteBreakpoints(boolean toggleBreakPointStatus) {
        setAreBreakpointsMuted(toggleBreakPointStatus);
    }

    @Override
    public void stepOut() {
        dim.setReturnValue(Dim.STEP_OUT);
    }

    @Override
    public void stepInto() {
        dim.setReturnValue(Dim.STEP_INTO);
        //@todo dim.setBreakOnEnter(true); //possible bug because BP
    }

    @Override
    public void stepOver() {
        dim.setReturnValue(Dim.STEP_OVER);
    }

    @Override
    public void continueRun() {
        this.dim.go();
    }

    @Override
    public boolean isBreakpointAllowed(int lineNumber) {
        Dim.SourceInfo sourceInfo = dim.sourceInfo(this.filename);
        return sourceInfo.breakableLine(lineNumber);
    }

    @Override
    public void setBreakpoint(int lineNumber, boolean stopOnBreakpoint) {
        try {
            Dim.SourceInfo sourceInfo = dim.sourceInfo(this.filename);
            sourceInfo.breakpoint(lineNumber, stopOnBreakpoint);
            System.out.println("after set breakpoint -" + " line " + lineNumber + " changed to " + stopOnBreakpoint);
        } catch (Exception e) {
            logger.error("cannot assign breakpoint on line {0}", lineNumber);
        }
    }

    @Override
    public void setSyncSnapshot(BProgramSyncSnapshot syncSnapshot) {
        this.syncSnapshot = syncSnapshot;
    }

    @Override
    public void onStateChanged() {
        onStateChangedEvent.apply(debuggerStateHelper.generateDebuggerState(syncSnapshot, state, lastContextData));
    }

    //todo: remove
    public void getVars() {
        StringBuilder vars = new StringBuilder();
        Dim.ContextData currentContextData = dim.currentContextData();
        for (int i = 0; i < currentContextData.frameCount(); i++) {
            vars.append("Scope no: ").append(i).append("\n");
            Dim.StackFrame stackFrame = currentContextData.getFrame(i);
            NativeCall scope = (NativeCall) stackFrame.scope();
            Object[] objects = ((Scriptable) scope).getIds();
            List<String> arguments = Arrays.stream(objects).map(Object::toString).collect(Collectors.toList()).subList(1, objects.length);
            for (String arg : arguments) {
                Object res = ScriptableObject.getProperty(scope, arg);
                if (Undefined.instance != res)
                    vars.append(arg).append(" ").append(res).append("\n");
            }
        }
        System.out.println("Vars: \n" + vars);
    }

        /*
    old code just for reference
     */
    @Override
    public void getState() {
        onStateChanged();
    }

}