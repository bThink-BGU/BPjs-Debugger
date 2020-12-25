package il.ac.bgu.se.bp.engine;

import il.ac.bgu.se.bp.debugger.DebuggerCommand;
import org.mozilla.javascript.tools.debugger.GuiCallback;

import java.util.Map;

public interface DebuggerCallback<T> extends GuiCallback {
    void setupBreakpoint(Map<Integer, Boolean> breakpoints);
    T addCommand(DebuggerCommand future);
    T debuggerCommandToCallback(DebuggerCommand command);
}
