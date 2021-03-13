package il.ac.bgu.se.bp.debugger.runner;

import il.ac.bgu.se.bp.debugger.BPJsDebugger;
import il.ac.bgu.se.bp.debugger.state.BPDebuggerState;

public interface DebuggerSessionHandler<T> extends OnStateChangedHandler {
    void addNewRunExecution(String sessionId, T bProgramRunner);

    void addNewDebugExecution(String sessionId, BPJsDebugger bpProgramDebugger);

    boolean validateSessionId(String sessionId);

    boolean removeSession(String sessionId);

    void updateLastOperationTime(String sessionId);

    Void updateUserStateChange(String sessionId, BPDebuggerState debuggerState);
}