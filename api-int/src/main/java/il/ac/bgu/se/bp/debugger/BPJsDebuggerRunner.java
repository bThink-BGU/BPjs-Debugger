package il.ac.bgu.se.bp.debugger;

import java.util.Map;

public interface BPJsDebuggerRunner<T> extends Debugger<T> {

    void setup(Map<Integer, Boolean> breakpoints);
    boolean isSetup();
    void start(Map<Integer, Boolean> breakpoints);
    boolean isStarted();

    void startSync();
    void nextSync();
}
