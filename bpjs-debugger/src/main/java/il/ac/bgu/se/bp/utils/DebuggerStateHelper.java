package il.ac.bgu.se.bp.utils;

import com.google.gson.Gson;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.SyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;
import il.ac.bgu.se.bp.debugger.RunnerState;
import il.ac.bgu.se.bp.debugger.engine.SyncSnapshotHolder;
import il.ac.bgu.se.bp.socket.state.BPDebuggerState;
import il.ac.bgu.se.bp.socket.state.BThreadInfo;
import il.ac.bgu.se.bp.socket.state.EventInfo;
import il.ac.bgu.se.bp.socket.state.EventsStatus;
import org.apache.commons.lang3.ArrayUtils;
import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.debugger.Dim;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSets.none;


public class DebuggerStateHelper {
    private Set<Pair<String, Object>> recentlyRegisteredBT = null;
    private HashMap<String, Object> newBTInterpreterFrames = new HashMap<>();
    private BPDebuggerState lastState = null;
    private String currentRunningBT = null;
    private SyncSnapshotHolder<BProgramSyncSnapshot, BEvent> syncSnapshotHolder;

    public DebuggerStateHelper(SyncSnapshotHolder syncSnapshotHolder) {
        this.syncSnapshotHolder = syncSnapshotHolder;
    }

    public BPDebuggerState generateDebuggerState(BProgramSyncSnapshot syncSnapshot, RunnerState state, Dim.ContextData lastContextData, Dim.SourceInfo sourceInfo) {
        lastState = generateDebuggerStateInner(syncSnapshot, state, lastContextData, sourceInfo);
        return lastState;
    }

    private BPDebuggerState generateDebuggerStateInner(BProgramSyncSnapshot syncSnapshot, RunnerState state, Dim.ContextData lastContextData, Dim.SourceInfo sourceInfo) {
        Set<BThreadSyncSnapshot> bThreadSyncSnapshots = syncSnapshot.getBThreadSnapshots();
        List<BThreadInfo> bThreadInfoList = bThreadSyncSnapshots
                .stream()
                .map(bThreadSyncSnapshot -> createBThreadInfo(bThreadSyncSnapshot, state, lastContextData))
                .collect(Collectors.toList());

        if (state.getDebuggerState() == RunnerState.State.JS_DEBUG) {
            bThreadInfoList.addAll(getRecentlyAddedBTInfo(lastContextData));
        } else {
            this.recentlyRegisteredBT = null;
            this.newBTInterpreterFrames = null;
            this.currentRunningBT = null;
        }
        Set<SyncStatement> statements = syncSnapshot.getStatements();

        List<EventSet> wait = statements.stream().map(SyncStatement::getWaitFor).collect(Collectors.toList());
        List<EventSet> blocked = statements.stream().map(SyncStatement::getBlock).collect(Collectors.toList());
        List<BEvent> requested = statements.stream().map(SyncStatement::getRequest).flatMap(Collection::stream).collect(Collectors.toList());
        List<EventInfo> waitEvents = wait.stream().map((e) -> e.equals(none) ? null : new EventInfo(((BEvent) e).getName())).filter(Objects::nonNull).collect(Collectors.toList());
        List<EventInfo> blockedEvents = blocked.stream().map((e) -> e.equals(none) ? null : new EventInfo(((BEvent) e).getName())).filter(Objects::nonNull).collect(Collectors.toList());
        Set<EventInfo> requestedEvents = requested.stream().map((e) -> new EventInfo(e.getName())).collect(Collectors.toSet());

        EventsStatus eventsStatus = new EventsStatus(waitEvents, blockedEvents, requestedEvents);
        Integer lineNumber = lastContextData == null ? null : lastContextData.frameCount() > 0 ? lastContextData.getFrame(0).getLineNumber() : null;
        SortedMap<Long, BEvent> events = this.syncSnapshotHolder.getEventsHistoryStack(0, 10);
        SortedMap<Long,EventInfo> eventsHistory = new TreeMap<>(Collections.reverseOrder());
        for(Map.Entry entry : events.entrySet()){
            eventsHistory.put((Long) entry.getKey(), new EventInfo( ((BEvent)entry.getValue()).name));
        }
        try{
            boolean[] breakpoints =  (boolean[])getValue(sourceInfo, "breakpoints");
            return new BPDebuggerState(bThreadInfoList, eventsStatus, eventsHistory, currentRunningBT, lineNumber, ArrayUtils.toObject(breakpoints));
        }
        catch (Exception e){
            System.out.println(e);
        }
        return new BPDebuggerState(bThreadInfoList, eventsStatus, eventsHistory, currentRunningBT, lineNumber);
    }

    private List<BThreadInfo> getRecentlyAddedBTInfo(Dim.ContextData lastContextData) {
        List<BThreadInfo> bThreadInfoList = new ArrayList<>();
        Context cx = Context.getCurrentContext();
        try {
            Object lastInterpreterFrame = getValue(cx, "lastInterpreterFrame");
//            Object fnOrScript = lastInterpreterFrame == null ? null : getValue(lastInterpreterFrame, "fnOrScript");
            Object fnOrScript = lastInterpreterFrame == null ? null : getBaseFnOrScript(lastInterpreterFrame);

            for (Pair<String, Object> recentlyRegisteredPair : recentlyRegisteredBT) {
                Object o = recentlyRegisteredPair.getRight();
                String btName = recentlyRegisteredPair.getLeft();
                if (o == fnOrScript) { // current running bt
                    Map<Integer, Map<String, String>> env = getEnvDebug(lastInterpreterFrame, lastContextData, btName);
                    newBTInterpreterFrames.put(btName, lastInterpreterFrame);
                    bThreadInfoList.add(new BThreadInfo(btName, env));
                } else {
                    Object savedInterpreterFrame = newBTInterpreterFrames.get(btName);
                    Map<Integer, Map<String, String>> env = savedInterpreterFrame == null ? new HashMap<>() :
                            getEnvDebug(savedInterpreterFrame, lastContextData, btName);
                    bThreadInfoList.add(new BThreadInfo(btName, env));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bThreadInfoList;
    }

    private Object getBaseFnOrScript(Object lastInterpreterFrame) {
        Object frame = lastInterpreterFrame;

        Object parentFrame = lastInterpreterFrame;
        try{
            while(parentFrame!= null){
                frame = parentFrame;
                parentFrame = getValue(frame, "parentFrame");
            }
            return getValue(frame, "fnOrScript");
        }
        catch (Exception e){
            return null;
        }

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

    /**
     * This function generating bthread env in JS debug state
     *
     * @param interpreterCallFrame
     * @param lastContextData
     * @param btName
     * @return
     */
    private Map<Integer, Map<String, String>> getEnvDebug(Object interpreterCallFrame, Dim.ContextData lastContextData, String btName) {
        Map<Integer, Map<String, String>> env = new HashMap<>();
        Integer key = 0;
        Context cx = Context.getCurrentContext();
        boolean currentRunningBT = false;
        try {
            Object lastInterpreterFrame = getValue(cx, "lastInterpreterFrame");
            if (lastInterpreterFrame == null)
                return getEnv(interpreterCallFrame, lastContextData);
            Object parentFrame = getValue(lastInterpreterFrame, "parentFrame");
            ScriptableObject interruptedScope = parentFrame != null ? (ScriptableObject) getValue(parentFrame, "scope") :
                    (ScriptableObject) getValue(lastInterpreterFrame, "scope");
            ScriptableObject paramScope = (ScriptableObject) getValue(interpreterCallFrame, "scope");
            if (paramScope == interruptedScope) { //current running BT
                currentRunningBT = true;
                for (int i = 0; i < lastContextData.frameCount(); i++) {
                    ScriptableObject scope = (ScriptableObject) lastContextData.getFrame(i).scope();
                    env.put(i, getScope(scope));
                }
                key = lastContextData.frameCount();
                this.currentRunningBT = btName;
            }
            parentFrame = interpreterCallFrame;
            while (parentFrame != null) {
                if (currentRunningBT) {
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
        Map<String, String> myEnv = new LinkedHashMap<>();
        try {
            Object function = getValue(scope, "function");
            Object interpretedData = getValue(function, "idata");
            String itsName = (String) getValue(interpretedData, "itsName");
            myEnv.put("FUNCNAME", itsName != null ? itsName : "BTMain");
            Object[] ids = Arrays.stream(scope.getIds()).filter((p) -> !p.toString().equals("arguments") && !p.toString().equals(itsName + "param")).toArray();
            for (Object id : ids) {
                Object jsValue = collectJsValue(scope.get(id));
                Gson gson = new Gson();
                myEnv.put(id.toString(), gson.toJson(jsValue));
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

    public BPDebuggerState peekNextState(BProgramSyncSnapshot syncSnapshot, RunnerState state, Dim.ContextData lastContextData,Dim.SourceInfo sourceInfo ) {
        return generateDebuggerStateInner(syncSnapshot, state, lastContextData, sourceInfo);
    }
}
