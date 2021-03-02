package il.ac.bgu.se.bp.debugger.engine;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.SyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;
import il.ac.bgu.se.bp.debugger.commands.DebuggerCommand;
import il.ac.bgu.se.bp.execution.RunnerState;
import il.ac.bgu.se.bp.logger.Logger;
import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.debugger.Dim;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

public class DebuggerEngineImpl implements DebuggerEngine<FutureTask<String>, String> {
    private final Logger logger = new Logger(DebuggerEngineImpl.class);
    private Dim dim;
    private final BlockingQueue<FutureTask<String>> queue;
    private final String filename;
    private Dim.ContextData lastContextData = null;
    private volatile boolean isRunning;
    private RunnerState state;
    private volatile boolean areBreakpointsMuted = false;
    private BProgramSyncSnapshot syncSnapshot = null;

    public DebuggerEngineImpl(String filename, RunnerState state) {
        this.filename = filename;
        this.state = state;
        queue = new ArrayBlockingQueue<>(1);
        dim = new Dim();
        dim.setGuiCallback(this);
        dim.attachTo(ContextFactory.getGlobal());
        setIsRunning(true);
    }

    public void setupBreakpoint(Map<Integer, Boolean> breakpoints) {
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
        BPDebuggerState bpDebuggerState = generateDebuggerState();
        logger.debug("Get state from enterInterrupt");

//        System.out.println(bpDebuggerState.toString());
        //send state via socket
        if (areBreakpointsMuted) {
            continueRun();
            return;
        }
//        printEnv();
    }

    @Override
    public boolean isGuiEventThread() {
        return true;
    }

    @Override
    public void dispatchNextGuiEvent() throws InterruptedException {
        logger.debug("Get state from dispatchNextGuiEvent");

        BPDebuggerState bpDebuggerState = generateDebuggerState();
        //send state via socket
        queue.take().run();
    }

    public FutureTask<String> addCommand(DebuggerCommand<FutureTask<String>, String> command) {
        FutureTask<String> futureTask;
        if (this.state.getDebuggerState() == RunnerState.State.JS_DEBUG) {
            futureTask = debuggerCommandToCallback(command);
            queue.add(futureTask);
            return futureTask;
        }
        futureTask = new FutureTask<>(() -> "Must be in js debug in order to execute this command");
        futureTask.run();
        return futureTask;
    }

    public FutureTask<String> debuggerCommandToCallback(DebuggerCommand<FutureTask<String>, String> command) {
        if (!isRunning()) {
            FutureTask<String> futureTask = new FutureTask<>(() -> "not running");
            futureTask.run();
            return futureTask;
        }
        return command.applyCommand(this);
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

    public String stop() {
        dim = null;
        setIsRunning(false);
        return "stopped";
    }

    public String toggleMuteBreakpoints(boolean toggleBreakPointStatus) {
        setAreBreakpointsMuted(toggleBreakPointStatus);
        return "breakpoints muted toggled to " + this.areBreakpointsMuted;
    }

    public String stepOut() {
        dim.setReturnValue(Dim.STEP_OUT);
        return "step into";
        //        return getDebuggerStatus();
    }

    public String stepInto() {
        dim.setReturnValue(Dim.STEP_INTO);
        //@todo dim.setBreakOnEnter(true); //possible bug because BP
        return "step into";
        //        return getDebuggerStatus();
    }

    public String stepOver() {
        dim.setReturnValue(Dim.STEP_OVER);
        return "step over";
        //        return getDebuggerStatus();
    }

    public String exit() {
        dim.setReturnValue(Dim.EXIT);
        return "exit";
        //        return getDebuggerStatus();
    }

    public String continueRun() {
        this.dim.go();
        return "continue run";
//        return getDebuggerStatus();
    }

    public String setBreakpoint(int lineNumber, boolean stopOnBreakpoint) {
        try {
            Dim.SourceInfo sourceInfo = dim.sourceInfo(this.filename);
            sourceInfo.breakpoint(lineNumber, stopOnBreakpoint);
            return "after set breakpoint -" + " line " + lineNumber + " changed to " + stopOnBreakpoint;
        } catch (Exception e) {
            logger.error("cannot assign breakpoint on line {0}", lineNumber);
            return null;
        }
    }

    /*
    old code just for reference
     */
    public String getVars() {
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
        return "Vars: \n" + vars;
    }

    @Override
    public String getState() {
        System.out.println();
        return "get state";
    }

    private Object getValue(Object instance, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field fld = instance.getClass().getDeclaredField(fieldName);
        fld.setAccessible(true);
        return fld.get(instance);
    }

    /**
     * Take a Javascript value from Rhino, build a Java value for it.
     *
     * @param jsValue
     * @return
     */
    private Object collectJsValue(Object jsValue) {
        if (jsValue == null) {
            return null;

        } else if (jsValue instanceof NativeFunction) {
            return ((NativeFunction) jsValue).getEncodedSource();

        } else if (jsValue instanceof NativeArray) {
            NativeArray jsArr = (NativeArray) jsValue;
            List<Object> retVal = new ArrayList<>((int) jsArr.getLength());
            for (int idx = 0; idx < jsArr.getLength(); idx++) {
                retVal.add(collectJsValue(jsArr.get(idx)));
            }
            return retVal;

        } else if (jsValue instanceof ScriptableObject) {
            ScriptableObject jsObj = (ScriptableObject) jsValue;
            Map<Object, Object> retVal = new HashMap<>();
            for (Object key : jsObj.getIds()) {
                retVal.put(key, collectJsValue(jsObj.get(key)));
            }
            return retVal;

        } else if (jsValue instanceof ConsString) {
            return ((ConsString) jsValue).toString();

        } else if (jsValue instanceof NativeJavaObject) {
            NativeJavaObject jsJavaObj = (NativeJavaObject) jsValue;
            Object obj = jsJavaObj.unwrap();
            return obj;

        } else {
            return jsValue;
        }

    }

    public Map<String, String> getScope(ScriptableObject scope) {
        Map<String, String> myEnv = new HashMap<>();
        try {
            Object function = getValue(scope, "function");
            Object interpeterData = getValue(function, "idata");
            String itsName = (String) getValue(interpeterData, "itsName");
            myEnv.put("FUNCNAME", itsName != null ? itsName : "BTMain");
            Object[] ids = Arrays.stream(scope.getIds()).filter((p) -> !p.toString().equals("arguments") && !p.toString().equals(itsName+"param")).toArray();
            for (Object id : ids) {
                myEnv.put(id.toString(), Objects.toString(collectJsValue(scope.get(id))));
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return myEnv;
    }

    private void printEnv() {
        Map<Integer, Map<String, String>> env = getEnv(this.lastContextData, null);
        for (Map.Entry e : env.entrySet()) {
            System.out.println(e.getKey() + ":" + e.getValue());
        }
    }

    public Map<Integer, Map<String, String>> getEnvDebug(Dim.ContextData contextData, Object interpreterCallFrame) {
        Map<Integer, Map<String, String>> env = new HashMap<>();
        Integer key = 0;
        Context cx = Context.getCurrentContext();
        boolean currentBT = false;
        try {
            Object lastInterpreterFrame = getValue(cx, "lastInterpreterFrame");
            Object parentFrame = getValue(lastInterpreterFrame, "parentFrame");
            ScriptableObject interruptedScope = (ScriptableObject) getValue(parentFrame, "scope");
            ScriptableObject paramScope = (ScriptableObject) getValue(interpreterCallFrame, "scope");
            if(paramScope == interruptedScope) //current running bthread
            {
                currentBT = true;
                for (int i = 0; i < lastContextData.frameCount(); i++) {
                    ScriptableObject scope = (ScriptableObject) lastContextData.getFrame(i).scope();
                    env.put(i, getScope(scope));
                }
                key = lastContextData.frameCount();
            }
            parentFrame = interpreterCallFrame;
            while (parentFrame != null) {
                if(currentBT){
                    Dim.ContextData debuggerFrame = ((Dim.StackFrame) getValue(parentFrame, "debuggerFrame")).contextData();
                    for (int i = 0; i < debuggerFrame.frameCount(); i++) {
                        ScriptableObject scope = (ScriptableObject) debuggerFrame.getFrame(i).scope();
                        env.put(key, getScope(scope));
                    }
                    key += debuggerFrame.frameCount();
                }
                else{
                    ScriptableObject scope = (ScriptableObject) getValue(parentFrame, "scope");
                    env.put(key, getScope(scope));
                    key += 1;
                }
                parentFrame = getValue(parentFrame, "parentFrame");
            }

        }catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return env;
    }

    public Map<Integer, Map<String, String>> getEnv(Dim.ContextData contextData, Object interpreterCallFrame) {
        Map<Integer, Map<String, String>> env = new HashMap<>();
        for (int i = 0; i < contextData.frameCount(); i++) {
            ScriptableObject scope = (ScriptableObject) contextData.getFrame(i).scope();
            env.put(i, getScope(scope));
        }
        Integer key =  1;
        try {
            Object lastFrame = interpreterCallFrame;
            ScriptableObject scope = (ScriptableObject) getValue(lastFrame, "scope");
            env.put(0, getScope(scope));
            Object parentFrame = getValue(lastFrame, "parentFrame");
            while (parentFrame != null) {
                Dim.ContextData debuggerFrame = ((Dim.StackFrame) getValue(parentFrame, "debuggerFrame")).contextData();
                scope = (ScriptableObject) getValue(parentFrame, "scope");
                env.put(key, getScope(scope));
                key += 1;
                parentFrame = getValue(parentFrame, "parentFrame");
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return env;
    }

    public void setSyncSnapshot(BProgramSyncSnapshot syncSnapshot) {
        this.syncSnapshot = syncSnapshot;
    }

    private BThreadInfo createBThreadInfo(BThreadSyncSnapshot bThreadSS) {
        Map<Integer, Map<String, String>> env;
        ScriptableObject scope = (ScriptableObject) bThreadSS.getScope();
        try {
            Object implementation = getValue(scope, "implementation");
            Dim.StackFrame debuggerFrame = (Dim.StackFrame) getValue(implementation, "debuggerFrame");
            env = state.getDebuggerState() == RunnerState.State.JS_DEBUG? getEnvDebug(debuggerFrame.contextData(), implementation) : getEnv(debuggerFrame.contextData(), implementation);
            EventSet wait = bThreadSS.getSyncStatement().getWaitFor();
            EventSet blocked = bThreadSS.getSyncStatement().getBlock();
            List<BEvent> requested = new ArrayList<>(bThreadSS.getSyncStatement().getRequest());
            return new BThreadInfo(bThreadSS.getName(), env, wait, blocked, requested);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public BPDebuggerState generateDebuggerState() {
        List<BThreadInfo> bThreadInfoList = new ArrayList<>();
        Set<BThreadSyncSnapshot> bThreadSyncSnapshot = syncSnapshot.getBThreadSnapshots();
        for (BThreadSyncSnapshot bThreadSS : bThreadSyncSnapshot) {
            bThreadInfoList.add(createBThreadInfo(bThreadSS));
        }
        EventsStatus eventsStatus = new EventsStatus(syncSnapshot.getStatements());
        return new BPDebuggerState(bThreadInfoList, eventsStatus);
    }

}
