package il.ac.bgu.se.bp.execution;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.se.bp.engine.DebuggerEngine;
import org.mozilla.javascript.tools.debugger.Dim;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Runs a {@link BProgram} in debug mode.
 */
public class BPJsDebuggerRunner{
    private DebuggerEngine debuggerEngine= null;
    private  BlockingQueue queue;
    String filename= "BPJSDebuggerTest.js";
    public BPJsDebuggerRunner() {
        this.queue = new ArrayBlockingQueue<String>(1);
        this.debuggerEngine = new DebuggerEngine(queue, filename);

    }

    public void start() {
        final BProgram bprog = new ResourceBProgram(filename);
        BProgramRunner rnr = new BProgramRunner();
        rnr.addListener(new PrintBProgramRunnerListener());
        rnr.setBProgram(bprog);
        bprog.setup();
        Dim dim = this.debuggerEngine.getDim();
        Dim.SourceInfo sourceInfo = dim.sourceInfo(filename);
        sourceInfo.breakpoint(4, true);
        new Thread(rnr).start();

    }

    public void setBreakPoint(int lineno) {
        try {
            queue.put("breakpoint " + lineno);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void removeBreakPoint(int lineno) {
        try {
            queue.put("remove " + lineno);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void go() {
        try {
            queue.put("go");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    // Service
    // BPjsDebuggerRunner Notifier - setup, new Thread(run) - program halt line 2 -> enter interrupt -> update data structure (apply callback) -> DebuggerEngine Listener sleep in dispatcher- wait to new commands
    // Service - go to next break point
}
