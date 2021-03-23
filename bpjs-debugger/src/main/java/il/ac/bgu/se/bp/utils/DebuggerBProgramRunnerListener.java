package il.ac.bgu.se.bp.utils;

import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.FailedAssertion;

import java.util.HashSet;
import java.util.Set;

public class DebuggerBProgramRunnerListener implements BProgramRunnerListener {
    private DebuggerStateHelper debuggerStateHelper;
    private Set<Pair<String, Object>> recentlyRegisteredBT = new HashSet<>();
    public DebuggerBProgramRunnerListener(DebuggerStateHelper debuggerStateHelper) {
        this.debuggerStateHelper = debuggerStateHelper;
    }
    @Override
    public void starting(BProgram bprog) {
        this.debuggerStateHelper.setRecentlyRegisteredBthreads(recentlyRegisteredBT);
        this.recentlyRegisteredBT.clear();
    }

    @Override
    public void started(BProgram bp) {

    }

    @Override
    public void superstepDone(BProgram bp) {

    }

    @Override
    public void ended(BProgram bp) {

    }

    @Override
    public void assertionFailed(BProgram bp, FailedAssertion theFailedAssertion) {

    }

    @Override
    public void bthreadAdded(BProgram bp, BThreadSyncSnapshot theBThread) {
        this.recentlyRegisteredBT.add(new Pair<>(theBThread.getName(), theBThread.getEntryPoint()));
    }

    @Override
    public void bthreadRemoved(BProgram bp, BThreadSyncSnapshot theBThread) {

    }

    @Override
    public void bthreadDone(BProgram bp, BThreadSyncSnapshot theBThread) {

    }

    @Override
    public void eventSelected(BProgram bp, BEvent theEvent) {
        this.debuggerStateHelper.setRecentlyRegisteredBthreads(recentlyRegisteredBT);
        this.recentlyRegisteredBT.clear();
    }

    @Override
    public void error(BProgram bp, Exception ex) {

    }

    @Override
    public void halted(BProgram bp) {

    }

}
