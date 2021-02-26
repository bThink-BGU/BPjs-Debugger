package il.ac.bgu.se.bp.debugger.commands;

import il.ac.bgu.se.bp.debugger.engine.DebuggerEngine;

public interface DebuggerCommand<T, S> {
    T applyCommand(DebuggerEngine<T, S> debugger);
}