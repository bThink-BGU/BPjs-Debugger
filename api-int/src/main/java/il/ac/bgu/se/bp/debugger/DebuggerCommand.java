package il.ac.bgu.se.bp.debugger;

public interface DebuggerCommand<T, S> {
    T applyCommand(DebuggerEngine<T, S> debugger);
}