package il.ac.bgu.se.bp.engine;

import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.tools.debugger.Dim;

public class DebuggerEngine implements DebuggerCallback {
    private Dim dim;

    public DebuggerEngine() {
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

    }

    @Override
    public boolean isGuiEventThread() {
        return false;
    }

    @Override
    public void dispatchNextGuiEvent() throws InterruptedException {

    }

    public void setDim(Dim dim){
        this.dim= dim;
    }

    public Dim getDim() {
        return dim;
    }
}
