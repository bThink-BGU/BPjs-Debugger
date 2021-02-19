package il.ac.bgu.se.bp.execution;

public class RunnerState {
    public enum State {
        Stopped,
        Running,
        SyncState,
        JSDebug
    }
    private State debuggerState;

    public RunnerState(State debuggerState) {
        synchronized (this){
            this.debuggerState = debuggerState;
        }
    }
    public RunnerState() {
        this.debuggerState = State.Stopped;
    }

    public synchronized State getDebuggerState() {
        return debuggerState;
    }

    public synchronized void setDebuggerState(State debuggerState) {
        this.debuggerState = debuggerState;
    }
}
