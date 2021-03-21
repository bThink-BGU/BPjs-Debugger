package il.ac.bgu.se.bp.service;

import il.ac.bgu.se.bp.debugger.BPJsDebugger;
import il.ac.bgu.se.bp.debugger.runner.BPjsProgramExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@EnableAsync
@Component
public class BPjsProgramExecutorImpl implements BPjsProgramExecutor<BPJsDebugger> {

    @Async
    @Override
    public void debugProgram(BPJsDebugger bProg, List<Integer> breakpoints,
                             boolean isStopOnBreakpointsToggle, boolean isStopOnSyncStateToggle) {
        Map<Integer, Boolean> breakpointsMap = breakpoints
                .stream()
                .collect(Collectors.toMap(Function.identity(), b -> Boolean.TRUE));
        bProg.setup(breakpointsMap, isStopOnBreakpointsToggle, isStopOnSyncStateToggle);
        bProg.startSync(isStopOnBreakpointsToggle, isStopOnSyncStateToggle);
    }

    @Async
    @Override
    public void nextSync(BPJsDebugger bProg) {
        bProg.nextSync();
    }

    @Async
    @Override
    public void continueRun(BPJsDebugger bProg) {
        bProg.continueRun();
    }
}
