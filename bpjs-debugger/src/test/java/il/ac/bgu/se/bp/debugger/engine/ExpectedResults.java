package il.ac.bgu.se.bp.debugger.engine;

import il.ac.bgu.se.bp.debugger.state.BPDebuggerState;
import il.ac.bgu.se.bp.debugger.state.BThreadInfo;
import il.ac.bgu.se.bp.debugger.state.EventsStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpectedResults {
    public static BPDebuggerState testEnvChangedInBreakPoints_ENV1(){
        List<BThreadInfo> bThreadInfoList = new ArrayList<>();
        Map<Integer, Map<String, String>> bt1Env =  new HashMap<>();
        HashMap<String, String> env = new HashMap<>();
        env.put("myvar1", "null");
        env.put("z", "null");
        env.put("FUNCNAME", "BTMain");
        bt1Env.put(0, env);
        Map<Integer, Map<String, String>> bt2Env =  new HashMap<>();
        bThreadInfoList.add(new BThreadInfo("bt-test-1", bt1Env));
        bThreadInfoList.add(new BThreadInfo("bt-test-2", bt2Env));
        return new BPDebuggerState(bThreadInfoList, new EventsStatus());
    }
    public static BPDebuggerState testEnvChangedInBreakPoints_ENV2(){
        List<BThreadInfo> bThreadInfoList = new ArrayList<>();
        Map<Integer, Map<String, String>> bt1Env =  new HashMap<>();
        HashMap<String, String> env = new HashMap<>();
        env.put("myvar1", "10.0");
        env.put("z", "null");
        env.put("FUNCNAME", "BTMain");
        bt1Env.put(0, env);
        Map<Integer, Map<String, String>> bt2Env =  new HashMap<>();
        bThreadInfoList.add(new BThreadInfo("bt-test-1", bt1Env));
        bThreadInfoList.add(new BThreadInfo("bt-test-2", bt2Env));
        return new BPDebuggerState(bThreadInfoList, new EventsStatus());
    }
}
