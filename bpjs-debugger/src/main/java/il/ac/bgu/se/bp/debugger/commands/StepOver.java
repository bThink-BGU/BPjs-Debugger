package il.ac.bgu.se.bp.debugger.commands;

import il.ac.bgu.se.bp.debugger.engine.DebuggerEngine;

import java.util.concurrent.FutureTask;

public class StepOver implements DebuggerCommand<FutureTask<String>, String> {
    @Override
    public FutureTask<String> applyCommand(DebuggerEngine<FutureTask<String>, String> debugger) {
        return new FutureTask<>(debugger::stepOver);
    }
}