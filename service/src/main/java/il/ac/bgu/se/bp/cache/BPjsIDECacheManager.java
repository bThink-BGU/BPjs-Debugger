package il.ac.bgu.se.bp.cache;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.se.bp.debugger.BPJsDebugger;

public interface BPjsIDECacheManager {
    void addNewRunExecution(String userId, BProgramRunner bProgramRunner);

    void updateLastOperationTime(String userId);

    void addNewDebugExecution(String userId, BPJsDebugger bpProgramDebugger);
}