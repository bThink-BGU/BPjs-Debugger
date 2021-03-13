package il.ac.bgu.se.bp.debugger.runner;

import il.ac.bgu.se.bp.debugger.state.BPDebuggerState;

public interface OnStateChangedHandler {
    void addUser(String sessionId, String userId);

    void sendMessages();        //temp

    void sendMessage(String sessionId, BPDebuggerState debuggerState);
}
