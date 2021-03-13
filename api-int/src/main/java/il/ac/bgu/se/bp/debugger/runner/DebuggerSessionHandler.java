package il.ac.bgu.se.bp.debugger.runner;

import il.ac.bgu.se.bp.debugger.BPJsDebugger;
import il.ac.bgu.se.bp.debugger.state.BPDebuggerState;

public interface DebuggerSessionHandler<T> extends OnStateChangedHandler {
    void addNewRunExecution(String userId, T bProgramRunner);

    void addNewDebugExecution(String userId, BPJsDebugger bpProgramDebugger);

    boolean validateUserId(String userId);

    boolean removeUser(String userId);

    void updateLastOperationTime(String userId);

    Void updateUserStateChange(String userId, BPDebuggerState debuggerState);
}