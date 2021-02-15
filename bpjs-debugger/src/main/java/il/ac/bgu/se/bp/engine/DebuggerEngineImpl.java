package il.ac.bgu.se.bp.engine;

import il.ac.bgu.cs.bp.bpjs.internal.ScriptableUtils;
import il.ac.bgu.se.bp.debugger.DebuggerCommand;
import il.ac.bgu.se.bp.logger.Logger;
import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.debugger.Dim;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

public class DebuggerEngineImpl implements DebuggerEngine<FutureTask<String>> {
    private final Logger logger = new Logger(DebuggerEngineImpl.class);
    private final Dim dim;
    private final BlockingQueue<FutureTask<String>> queue;
    private final String filename;
    private Dim.ContextData lastContextData = null;

    public DebuggerEngineImpl(String filename) {
        this.filename = filename;
        queue = new ArrayBlockingQueue<>(1);
        dim = new Dim();
        dim.setGuiCallback(this);
        dim.attachTo(ContextFactory.getGlobal());
    }

    public void setupBreakpoint(Map<Integer, Boolean> breakpoints) {
        breakpoints.forEach(this::setBreakpoint);
    }

    @Override
    public void updateSourceText(Dim.SourceInfo sourceInfo) {
    }

    private Object getValue(Object instance, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field fld = instance.getClass().getDeclaredField(fieldName);
        fld.setAccessible(true);
        return fld.get(instance);
    }

    @Override
    public void enterInterrupt(Dim.StackFrame stackFrame, String s, String s1) {
        System.out.println("Breakpoint reached- " + s + " Line no: " + stackFrame.getLineNumber());
        this.lastContextData = stackFrame.contextData();
        for (int i = 0; i < this.lastContextData.frameCount(); i++) {
            System.out.println(ScriptableUtils.toString((Scriptable) this.lastContextData.getFrame(i).scope()));
        }


        Context cx = Context.getCurrentContext();
        try {
            Object lastFrame = getValue(cx, "lastInterpreterFrame");
            Object parentFrame = getValue(lastFrame, "parentFrame");
            if (parentFrame != null) {
                Object debuggerFrame = getValue(parentFrame, "debuggerFrame");
                Scriptable scriptable = (Scriptable) getValue(debuggerFrame, "scope");
                if (debuggerFrame != this.lastContextData) {
                    System.out.println("print from last frame");
                    System.out.println(ScriptableUtils.toString(scriptable));
                }
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        // Update service -> we on breakpoint! (apply callback)
    }

    @Override
    public boolean isGuiEventThread() {
        return true;
    }

    @Override
    public void dispatchNextGuiEvent() throws InterruptedException {
        queue.take().run();
    }

    public FutureTask<String> addCommand(DebuggerCommand command) {
        FutureTask<String> futureTask = debuggerCommandToCallback(command);
        queue.add(futureTask);
        return futureTask;
    }

    public FutureTask<String> debuggerCommandToCallback(DebuggerCommand command) {
        Callable<String> callback = null;
        switch (command.getDebuggerOperation()) {
            case CONTINUE:
                callback = this::continueRun;
                break;
            case STEP_INTO:
                callback = this::stepInto;
                break;
            case STEP_OVER:
                callback = this::stepOver;
                break;
            case STEP_OUT:
                callback = this::stepOut;
                break;
            case EXIT:
                callback = this::exit;
                break;
            case SET_BREAKPOINT:
                callback = () -> this.setBreakpoint((Integer) command.getArgs()[0], true);
                break;
            case REMOVE_BREAKPOINT:
                callback = () -> this.setBreakpoint((Integer) command.getArgs()[0], false);
                break;
            case GET_VARS:
                callback = this::getVars;
                break;
        }
        return new FutureTask<>(callback);
    }

    private String stepOut() {
        dim.setReturnValue(Dim.STEP_OUT);
        return "step into";
        //        return getDebuggerStatus();
    }

    private String stepInto() {
        dim.setReturnValue(Dim.STEP_INTO);
        return "step into";
        //        return getDebuggerStatus();
    }

    private String stepOver() {
        dim.setReturnValue(Dim.STEP_OVER);
        return "step over";
        //        return getDebuggerStatus();
    }

    private String exit() {
        dim.setReturnValue(Dim.EXIT);
        return "exit";
        //        return getDebuggerStatus();
    }

    private String continueRun() {
        this.dim.go();
        return "continue run";
//        return getDebuggerStatus();
    }

    private String setBreakpoint(int lineNumber, boolean stopOnBreakpoint) {
        try {
            Dim.SourceInfo sourceInfo = dim.sourceInfo(this.filename);
            sourceInfo.breakpoint(lineNumber, stopOnBreakpoint);
            return "after set breakpoint -" + " line " + lineNumber + " changed to " + stopOnBreakpoint;
//        return getDebuggerStatus();
        } catch (Exception e) {
            logger.error("cannot assign breakpoint on line {0}", lineNumber);
            return null;
        }
    }

    private String getVars() {
        StringBuilder vars = new StringBuilder();
        Dim.ContextData currentContextData = dim.currentContextData();
        for (int i = 0; i < currentContextData.frameCount(); i++) {
            vars.append("Scope no: ").append(i).append("\n");
            Dim.StackFrame stackFrame = currentContextData.getFrame(i);
            NativeCall scope = (NativeCall) stackFrame.scope();

            Object[] objects = ((Scriptable) scope).getIds();
            List<String> arguments = Arrays.stream(objects).map(p -> p.toString()).collect(Collectors.toList()).subList(1, objects.length);
            for (String arg : arguments) {
                Object res = ScriptableObject.getProperty(scope, arg);
                if (Undefined.instance != res)
                    vars.append(arg).append(" ").append(res).append("\n");
            }
        }
        return "Vars: \n" + vars;
    }

}
