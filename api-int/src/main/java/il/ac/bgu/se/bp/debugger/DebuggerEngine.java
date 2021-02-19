package il.ac.bgu.se.bp.debugger;

import org.mozilla.javascript.tools.debugger.GuiCallback;

import java.util.Map;

public interface DebuggerEngine<T, S> extends GuiCallback {
    void setupBreakpoint(Map<Integer, Boolean> breakpoints);
    T addCommand(DebuggerCommand<T, S> future);
    T debuggerCommandToCallback(DebuggerCommand<T, S> command);

    S setBreakpoint(int lineNumber, boolean stopOnBreakpoint);
    S getVars();
    S stop();
    S stepOut();
    S stepInto();
    S stepOver();
    S exit();
    S continueRun();
}
