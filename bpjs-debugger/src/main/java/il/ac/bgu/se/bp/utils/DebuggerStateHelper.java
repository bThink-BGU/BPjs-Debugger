package il.ac.bgu.se.bp.utils;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.SyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;
import il.ac.bgu.se.bp.socket.state.BPDebuggerState;
import il.ac.bgu.se.bp.socket.state.BThreadInfo;
import il.ac.bgu.se.bp.socket.state.EventInfo;
import il.ac.bgu.se.bp.socket.state.EventsStatus;
import il.ac.bgu.se.bp.debugger.RunnerState;
import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.debugger.Dim;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSets.none;


public class DebuggerStateHelper {
    private Set<Pair<String, Object>> recentlyRegisteredBT = null;
    private HashMap<String, Object> newBTInterpeterFrames = new HashMap<>();
    private BPDebuggerState lastState = null;
    private BEvent lastChosenEvent;
    private String currentRunningBT = null;

    public void setLastChosenEvent(BEvent lastChosenEvent) {
        this.lastChosenEvent = lastChosenEvent;
    }

    public BPDebuggerState generateDebuggerState(BProgramSyncSnapshot syncSnapshot, RunnerState state, Dim.ContextData lastContextData) {
        lastState = generateDebuggerStateInner(syncSnapshot, state, lastContextData);
        return lastState;
    }

    private BPDebuggerState generateDebuggerStateInner(BProgramSyncSnapshot syncSnapshot, RunnerState state, Dim.ContextData lastContextData) {
        Set<BThreadSyncSnapshot> bThreadSyncSnapshots = syncSnapshot.getBThreadSnapshots();
        List<BThreadInfo> bThreadInfoList = bThreadSyncSnapshots
                .stream()
                .map(bThreadSyncSnapshot -> createBThreadInfo(bThreadSyncSnapshot, state, lastContextData))
                .collect(Collectors.toList());

        if (state.getDebuggerState() == RunnerState.State.JS_DEBUG) {
            bThreadInfoList.addAll(getRecentlyAddedBTInfo(state, lastContextData));
        } else {
            this.recentlyRegisteredBT = null;
            this.newBTInterpeterFrames = null;
            this.currentRunningBT= null;
        }
        Set<SyncStatement> statements = syncSnapshot.getStatements();

        List<EventSet> wait = statements.stream().map(SyncStatement::getWaitFor).collect(Collectors.toList());
        List<EventSet> blocked = statements.stream().map(SyncStatement::getBlock).collect(Collectors.toList());
        List<BEvent> requested = statements.stream().map(SyncStatement::getRequest).flatMap(Collection::stream).collect(Collectors.toList());
        List<EventInfo> waitEvents = wait.stream().map((e) -> e.equals(none) ? null : new EventInfo(((BEvent) e).getName())).filter(Objects::nonNull).collect(Collectors.toList());
        List<EventInfo> blockedEvents = blocked.stream().map((e) -> e.equals(none) ? null : new EventInfo(((BEvent) e).getName())).filter(Objects::nonNull).collect(Collectors.toList());
        Set<EventInfo> requestedEvents = requested.stream().map((e) -> new EventInfo(e.getName())).collect(Collectors.toSet());

        EventsStatus eventsStatus = new EventsStatus(waitEvents, blockedEvents, requestedEvents);
        EventInfo chosenEvent = lastChosenEvent != null ? new EventInfo(lastChosenEvent.getName()) : null;
        Integer lineNumber = lastContextData == null? null : lastContextData.frameCount() > 0 ? lastContextData.getFrame(0).getLineNumber() : null;

        return new BPDebuggerState(bThreadInfoList, eventsStatus, chosenEvent, currentRunningBT, lineNumber);
    }

    private List<BThreadInfo> getRecentlyAddedBTInfo(RunnerState state, Dim.ContextData lastContextData) {
        List<BThreadInfo> bThreadInfoList = new ArrayList<>();
        Context cx = Context.getCurrentContext();
        try {
            Object lastInterpreterFrame = getValue(cx, "lastInterpreterFrame");
            Object fnOrScript = lastInterpreterFrame == null ? null : getValue(lastInterpreterFrame, "fnOrScript");
            for (Pair<String, Object> recentlyRegisteredPair : recentlyRegisteredBT) {
                Object o = recentlyRegisteredPair.getRight();
                String btName = recentlyRegisteredPair.getLeft();
                if (o == fnOrScript) { // current running bt
                    Map<Integer, Map<String, String>> env = getEnvDebug(lastInterpreterFrame, lastContextData,btName);
                    newBTInterpeterFrames.put(btName, lastInterpreterFrame);
                    bThreadInfoList.add(new BThreadInfo(btName, env));
                } else {
                    Object savedInterpreterFrame = newBTInterpeterFrames.get(btName);
                    Map<Integer, Map<String, String>> env = savedInterpreterFrame == null ? new HashMap<>() :
                            getEnvDebug(savedInterpreterFrame, lastContextData,btName);
                    bThreadInfoList.add(new BThreadInfo(btName, env));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bThreadInfoList;
    }


    //return true if none of the bthreads that already registered is the owner of the cx interpeter frame
    private boolean isCurrentThreadsFirstRun(Set<BThreadSyncSnapshot> bThreadSyncSnapshots) {
        Context cx = Context.getCurrentContext();
        for (BThreadSyncSnapshot bThreadSS : bThreadSyncSnapshots) {
            ScriptableObject scope = (ScriptableObject) bThreadSS.getScope();
            try {
                Object implementation = getValue(scope, "implementation");
                Object lastInterpreterFrame = getValue(cx, "lastInterpreterFrame");
                Object parentFrame = getValue(lastInterpreterFrame, "parentFrame");
                ScriptableObject interruptedScope = parentFrame != null ? (ScriptableObject) getValue(parentFrame, "scope") :
                        (ScriptableObject) getValue(lastInterpreterFrame, "scope");
                ScriptableObject paramScope = (ScriptableObject) getValue(implementation, "scope");
                if (paramScope == interruptedScope) return false;

            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        return true;
    }


    private BThreadInfo createBThreadInfo(BThreadSyncSnapshot bThreadSS, RunnerState state, Dim.ContextData lastContextData) {
        ScriptableObject scope = (ScriptableObject) bThreadSS.getScope();
        try {
            Object implementation = getValue(scope, "implementation");
            Dim.StackFrame debuggerFrame = (Dim.StackFrame) getValue(implementation, "debuggerFrame");
            Map<Integer, Map<String, String>> env = state == null ? null :
                    state.getDebuggerState() == RunnerState.State.JS_DEBUG ? getEnvDebug(implementation, lastContextData, bThreadSS.getName()) :
                            getEnv(implementation, debuggerFrame.contextData());
            EventSet waitFor = bThreadSS.getSyncStatement().getWaitFor();
            EventInfo waitEvent = waitFor.equals(none) ? null : new EventInfo(((BEvent) waitFor).getName());
            EventSet blocked = bThreadSS.getSyncStatement().getBlock();
            EventInfo blockedEvent = blocked.equals(none) ? null : new EventInfo(((BEvent) blocked).getName());
            Set<EventInfo> requested = new ArrayList<>(bThreadSS.getSyncStatement().getRequest()).stream().map((r) -> new EventInfo(r.getName())).collect(Collectors.toSet());
            return new BThreadInfo(bThreadSS.getName(), env, waitEvent, blockedEvent, requested);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Object getValue(Object instance, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field fld = instance.getClass().getDeclaredField(fieldName);
        fld.setAccessible(true);
        return fld.get(instance);
    }

    private Map<Integer, Map<String, String>> getEnvDebug(Object interpreterCallFrame, Dim.ContextData lastContextData, String btName) {
        Map<Integer, Map<String, String>> env = new HashMap<>();
        Integer key = 0;
        Context cx = Context.getCurrentContext();
        boolean currentBT = false;
        try {
            Object lastInterpreterFrame = getValue(cx, "lastInterpreterFrame");
            if (lastInterpreterFrame == null)
                return getEnv(interpreterCallFrame, lastContextData);
            Object parentFrame = getValue(lastInterpreterFrame, "parentFrame");
            ScriptableObject interruptedScope = parentFrame != null ? (ScriptableObject) getValue(parentFrame, "scope") :
                    (ScriptableObject) getValue(lastInterpreterFrame, "scope");
            ScriptableObject paramScope = (ScriptableObject) getValue(interpreterCallFrame, "scope");
            if (paramScope == interruptedScope) { //current running bthread
                currentBT = true;
                for (int i = 0; i < lastContextData.frameCount(); i++) {
                    ScriptableObject scope = (ScriptableObject) lastContextData.getFrame(i).scope();
                    env.put(i, getScope(scope));
                }
                key = lastContextData.frameCount();
                this.currentRunningBT= btName;
            }
            parentFrame = interpreterCallFrame;
            while (parentFrame != null) {
                if (currentBT) {
                    Dim.ContextData debuggerFrame = ((Dim.StackFrame) getValue(parentFrame, "debuggerFrame")).contextData();
                    if (debuggerFrame != lastContextData) {
                        for (int i = 0; i < debuggerFrame.frameCount(); i++) {
                            ScriptableObject scope = (ScriptableObject) debuggerFrame.getFrame(i).scope();
                            env.put(key, getScope(scope));
                        }
                        key += debuggerFrame.frameCount();
                    }
                } else {
                    ScriptableObject scope = (ScriptableObject) getValue(parentFrame, "scope");
                    env.put(key, getScope(scope));
                    key += 1;
                }
                parentFrame = getValue(parentFrame, "parentFrame");
            }

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return env;
    }

    private Map<Integer, Map<String, String>> getEnv(Object interpreterCallFrame, Dim.ContextData contextData) {
        Map<Integer, Map<String, String>> env = new HashMap<>();
        for (int i = 0; i < contextData.frameCount(); i++) {
            ScriptableObject scope = (ScriptableObject) contextData.getFrame(i).scope();
            env.put(i, getScope(scope));
        }
        Integer key = 1;
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

    private Map<String, String> getScope(ScriptableObject scope) {
        Map<String, String> myEnv = new HashMap<>();
        try {
            Object function = getValue(scope, "function");
            Object interpeterData = getValue(function, "idata");
            String itsName = (String) getValue(interpeterData, "itsName");
            myEnv.put("FUNCNAME", itsName != null ? itsName : "BTMain");
            Object[] ids = Arrays.stream(scope.getIds()).filter((p) -> !p.toString().equals("arguments") && !p.toString().equals(itsName + "param")).toArray();
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

    public BPDebuggerState getLastState() {
        return lastState;
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

    public void setRecentlyRegisteredBthreads(Set<Pair<String, Object>> recentlyRegistered) {
        this.recentlyRegisteredBT = recentlyRegistered;
    }

    public BPDebuggerState peekNextState(BProgramSyncSnapshot syncSnapshot, RunnerState state, Dim.ContextData lastContextData) {
        return generateDebuggerStateInner(syncSnapshot, state, lastContextData);
    }
}
