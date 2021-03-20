package il.ac.bgu.se.bp.debugger.runner;

import il.ac.bgu.se.bp.debugger.BPJsDebugger;
import il.ac.bgu.se.bp.utils.observer.BPEvent;
import il.ac.bgu.se.bp.utils.observer.Subscriber;
import il.ac.bgu.se.bp.utils.visitor.PublisherVisitor;

public interface SessionHandler<T> extends PublisherVisitor, Subscriber<BPEvent> {
    void addNewRunExecution(String userId, T bProgramRunner);
    T getBPjsRunnerByUser(String userId);

    void addNewDebugExecution(String userId, BPJsDebugger bpProgramDebugger);
    BPJsDebugger getBPjsDebuggerByUser(String userId);


    void updateLastOperationTime(String userId);
    void removeUser(String userId);

    boolean validateUserId(String userId);

    void addUser(String sessionId, String userId);
}