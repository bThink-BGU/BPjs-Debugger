package il.ac.bgu.cs.bp.bpjs.debugger;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import org.mozilla.javascript.tools.debugger.Dim;
import org.mozilla.javascript.tools.debugger.GuiCallback;

public class ShellGui implements GuiCallback {
    Dim dim;
    BProgramRunner bProgramRunner;
    public void setDim(Dim dim){
        this.dim= dim;
    }
    public void setbProgramRunner(BProgramRunner bProgramRunner){
        this.bProgramRunner= bProgramRunner;
    }
    public void updateSourceText(Dim.SourceInfo sourceInfo) {
        String fileName = sourceInfo.url();
        System.out.println(fileName);
    }

    public void enterInterrupt(Dim.StackFrame stackFrame, String s, String s1) {
        System.out.println("Got interrupt! " + s);
        Dim.ContextData contextData = stackFrame.contextData();
        System.out.println(stackFrame.getLineNumber());

      //  this.dim.setReturnValue(3);
        // notify server the stack trace state
    }

    public boolean isGuiEventThread() {
        return true;
    }

    public void dispatchNextGuiEvent() throws InterruptedException {
        this.dim.go();
    }
}
