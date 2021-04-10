package il.ac.bgu.se.bp.mocks;

import il.ac.bgu.se.bp.service.manage.SessionHandlerImpl;
import il.ac.bgu.se.bp.socket.console.ConsoleMessage;
import il.ac.bgu.se.bp.socket.exit.ProgramExit;
import il.ac.bgu.se.bp.socket.state.BPDebuggerState;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SessionHandlerMock extends SessionHandlerImpl {
    private ConcurrentMap<String, List<BPDebuggerState>> debuggerStatesPerUser = new ConcurrentHashMap<>();
    private ConcurrentMap<String, List<ConsoleMessage>> consoleMessagesPerUser = new ConcurrentHashMap<>();

    @Override
    public void visit(String userId, BPDebuggerState debuggerState) {
        debuggerStatesPerUser.putIfAbsent(userId, new LinkedList<>());
        debuggerStatesPerUser.get(userId).add(debuggerState);
    }

    @Override
    public void visit(String userId, ConsoleMessage consoleMessage) {
        System.out.println(userId + ": " + consoleMessage.getMessage());
        consoleMessagesPerUser.putIfAbsent(userId, new LinkedList<>());
        consoleMessagesPerUser.get(userId).add(consoleMessage);
    }

    @Override
    public void visit(String userId, ProgramExit programExit) {

    }

    public void cleanMockData() {
        debuggerStatesPerUser.clear();
        consoleMessagesPerUser.clear();
    }

    public void cleanUserMockData(String userId) {
        debuggerStatesPerUser.getOrDefault(userId, new LinkedList<>()).clear();
        consoleMessagesPerUser.getOrDefault(userId, new LinkedList<>()).clear();
    }


    public List<BPDebuggerState> getUsersDebuggerStates(String userId) {
        return debuggerStatesPerUser.get(userId);
    }

    public BPDebuggerState getUsersLastDebuggerState(String userId) {
        return getLastIfNotEmpty(getUsersDebuggerStates(userId));
    }

    public List<ConsoleMessage> getUsersConsoleMessages(String userId) {
        return consoleMessagesPerUser.get(userId);
    }

    public ConsoleMessage getUsersLastConsoleMessage(String userId) {
        return getLastIfNotEmpty(getUsersConsoleMessages(userId));
    }

    private <T> T getLastIfNotEmpty(List<T> list) {
        return list == null || list.isEmpty() ? null : list.get(list.size() - 1);
    }
}
