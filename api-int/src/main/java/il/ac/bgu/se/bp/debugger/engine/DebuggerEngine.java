package il.ac.bgu.se.bp.debugger.engine;

import il.ac.bgu.se.bp.debugger.Debugger;
import il.ac.bgu.se.bp.debugger.commands.DebuggerCommand;
import org.mozilla.javascript.tools.debugger.GuiCallback;

import java.util.Map;

public interface DebuggerEngine<T, S> extends GuiCallback, Debugger<S> {
    void setupBreakpoint(Map<Integer, Boolean> breakpoints);
    T addCommand(DebuggerCommand<T, S> future);
    T debuggerCommandToCallback(DebuggerCommand<T, S> command);
}
