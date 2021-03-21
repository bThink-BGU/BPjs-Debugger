package il.ac.bgu.se.bp.debugger.runner;

import il.ac.bgu.se.bp.rest.response.BooleanResponse;

public interface BPjsProgramValidator<T> {
    BooleanResponse validateNextSync(T bProg);
    BooleanResponse validateContinueRun(T bProg);
}
