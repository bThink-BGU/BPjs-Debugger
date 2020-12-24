package il.ac.bgu.se.bp.engine;

import il.ac.bgu.se.bp.debugger.DebuggerCommand;
import org.mozilla.javascript.tools.debugger.GuiCallback;

public interface DebuggerCallback<T> extends GuiCallback {
    T addCommand(DebuggerCommand future);
    public T debuggerCommandToCallback(DebuggerCommand command);
}
