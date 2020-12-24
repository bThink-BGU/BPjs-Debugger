package il.ac.bgu.se.bp.engine;

import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.tools.debugger.Dim;

import java.util.concurrent.BlockingQueue;

public class DebuggerEngine implements DebuggerCallback {
    private Dim dim;
    private final BlockingQueue<String> queue;
    private String filename;

    public DebuggerEngine(BlockingQueue queue, String filename) {
        this.queue = queue;
        this.filename = filename;
        this.dim = new Dim();
        dim.setGuiCallback(this);
        ContextFactory factory = ContextFactory.getGlobal();
        dim.attachTo(factory);
    }

    public void setup(){

    }

    @Override
    public void updateSourceText(Dim.SourceInfo sourceInfo) {

    }

    @Override
    public void enterInterrupt(Dim.StackFrame stackFrame, String s, String s1) {
        System.out.println("Got interrupt! " + s);
        Dim.ContextData contextData = stackFrame.contextData();
        System.out.println(stackFrame.getLineNumber());
        // Update service -> we on breakpoint! (apply callback)


    }

    @Override
    public boolean isGuiEventThread() {
        return true;
    }

    @Override
    public void dispatchNextGuiEvent() throws InterruptedException {
        System.out.println("trying to take");
        String nextCmd= queue.take();

        String[] splat = nextCmd.split(" ");
        switch (splat[0]) {
            case "go": {
                System.out.println("trying to take");
                this.dim.go();
                break;
            }
            case "break": {
                Dim.SourceInfo sourceInfo = dim.sourceInfo(this.filename);
                sourceInfo.breakpoint(Integer.parseInt(splat[1]), true);
                break;
            }
            case "remove": {
                Dim.SourceInfo sourceInfo = dim.sourceInfo(this.filename);
                sourceInfo.breakpoint(Integer.parseInt(splat[1]), false);
                break;
            }
            default: {
                break;
            }
        }
        // sleep until user enter next command...
    }

    public void setDim(Dim dim){
        this.dim= dim;
    }

    public Dim getDim() {
        return dim;
    }
}
