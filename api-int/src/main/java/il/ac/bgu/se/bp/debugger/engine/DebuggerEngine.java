package il.ac.bgu.se.bp.debugger.engine;

import il.ac.bgu.se.bp.debugger.commands.DebuggerCommand;
import org.mozilla.javascript.tools.debugger.GuiCallback;

import java.util.Map;

public interface DebuggerEngine<S> extends GuiCallback {
    boolean isBreakpointAllowed(int lineNumber);
    void setBreakpoint(int lineNumber, boolean stopOnBreakpoint);
    void stop();
    void stepOut();
    void stepInto();
    void stepOver();
    void continueRun();
    void toggleMuteBreakpoints(boolean toggleBreakPointStatus);
    void getState();

    void setSyncSnapshot(S syncSnapshot);
    void setupBreakpoints(Map<Integer, Boolean> breakpoints);
    void addCommand(DebuggerCommand command);

    void onStateChanged();
}
