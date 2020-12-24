package il.ac.bgu.se.bp.execution;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.se.bp.engine.DebuggerEngine;
import org.mozilla.javascript.tools.debugger.Dim;

/**
 * Runs a {@link BProgram} in debug mode.
 */
public class BPJsDebuggerRunner {
    private DebuggerEngine debuggerEngine= null;

    public BPJsDebuggerRunner() {
        this.debuggerEngine = new DebuggerEngine();
    }

    public void run(){
        String filename= "BPJSDebuggerTest.js";
        final BProgram bprog = new ResourceBProgram(filename);
        BProgramRunner rnr = new BProgramRunner();
        rnr.addListener(new PrintBProgramRunnerListener());
        rnr.setBProgram(bprog);


        rnr.run();
    }
    // Service
    // BPjsDebuggerRunner Notifier - setup, new Thread(run) - program halt line 2 -> enter interrupt -> update data structure (apply callback) -> DebuggerEngine Listener sleep in dispatcher- wait to new commands
    // Service - go to next break point
}
