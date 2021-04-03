package il.ac.bgu.se.bp.mocks;

import il.ac.bgu.se.bp.service.manage.SessionHandlerImpl;
import il.ac.bgu.se.bp.socket.console.ConsoleMessage;
import il.ac.bgu.se.bp.socket.exit.ProgramExit;
import il.ac.bgu.se.bp.socket.state.BPDebuggerState;

import java.util.LinkedList;
import java.util.List;

public class SessionHandlerMock extends SessionHandlerImpl {
    private List<BPDebuggerState> debuggerStates = new LinkedList<>();

    @Override
    public void visit(String userId, BPDebuggerState debuggerState) {
        debuggerStates.add(debuggerState);
    }

    @Override
    public void visit(String userId, ConsoleMessage consoleMessage) {

    }

    @Override
    public void visit(String userId, ProgramExit programExit) {

    }

    public void cleanMock() {
        this.debuggerStates.clear();
    }

    public List<BPDebuggerState> getDebuggerStates() {
        return debuggerStates;
    }

    public BPDebuggerState getLastDebuggerStates() {
        if (debuggerStates.isEmpty())
            return null;

        return debuggerStates.get(debuggerStates.size() - 1);
    }
}
