package il.ac.bgu.se.bp.debugger.commands;

import il.ac.bgu.se.bp.debugger.engine.DebuggerEngine;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;

import static il.ac.bgu.se.bp.utils.FutureHelper.createSuccessResult;

public class StepOut implements DebuggerCommand {
    @Override
    public BooleanResponse applyCommand(DebuggerEngine debugger) {
        debugger.stepOut();
        return createSuccessResult();
    }
}