package il.ac.bgu.se.bp.service.manage;

import il.ac.bgu.se.bp.debugger.BPJsDebugger;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;
import il.ac.bgu.se.bp.utils.observer.BPEvent;
import il.ac.bgu.se.bp.utils.observer.Subscriber;
import il.ac.bgu.se.bp.utils.visitor.PublisherVisitor;

public interface SessionHandler extends PublisherVisitor, Subscriber<BPEvent> {
    void addUser(String sessionId, String userId);

    void addNewRunExecution(String userId, BPJsDebugger<BooleanResponse> bProgramRunner, String filename);
    BPJsDebugger<BooleanResponse> getBPjsRunnerByUser(String userId);

    void addNewDebugExecution(String userId, BPJsDebugger<BooleanResponse> bpProgramDebugger, String filename);
    BPJsDebugger<BooleanResponse> getBPjsDebuggerByUser(String userId);
    BPJsDebugger<BooleanResponse> getBPjsDebuggerOrRunnerByUser(String userId);


    void updateLastOperationTime(String userId);
    void removeUser(String userId);

    boolean validateUserId(String userId);

    UserSession getUserSession(String userId);
}