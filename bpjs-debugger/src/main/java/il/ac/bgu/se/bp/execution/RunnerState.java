package il.ac.bgu.se.bp.execution;

public class RunnerState {
    public enum State {
        STOPPED,
        RUNNING,
        SYNC_STATE,
        WAITING_FOR_EXTERNAL_EVENT,
        JS_DEBUG
    }
    private State debuggerState;

    public RunnerState(State debuggerState) {
        synchronized (this){
            this.debuggerState = debuggerState;
        }
    }
    public RunnerState() {
        this.debuggerState = State.STOPPED;
    }

    public synchronized State getDebuggerState() {
        return debuggerState;
    }

    public synchronized void setDebuggerState(State debuggerState) {
        this.debuggerState = debuggerState;
    }
}
