package il.ac.bgu.se.bp.service.manage;

import java.util.List;

public interface BPjsProgramExecutor<T> {
    void debugProgram(T bProg, List<Integer> breakpoints, boolean isStopOnBreakpointsToggle, boolean isStopOnSyncStateToggle);
    void nextSync(T bProg);
    void continueRun(T bProg);
}