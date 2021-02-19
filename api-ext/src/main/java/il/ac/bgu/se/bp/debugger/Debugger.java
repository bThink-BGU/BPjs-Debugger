package il.ac.bgu.se.bp.debugger;

public interface Debugger<T> {
    T setBreakpoint(int lineNumber, boolean stopOnBreakpoint);
    T getVars();
    T stop();
    T stepOut();
    T stepInto();
    T stepOver();
    T exit();
    T continueRun();
    T toggleMuteBreakpoints();
}
