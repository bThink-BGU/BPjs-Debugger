package il.ac.bgu.se.bp.debugger;

public interface BPJsDebuggerRunner<T> {

    void start();
    T setBreakpoint(int lineNumber);
    T removeBreakpoint(int lineNumber);
    T continueRun();
    T stepInto();
    T stepOver();
    T stepOut();
    T getVars();
    T exit();
    void nextSync();

}
