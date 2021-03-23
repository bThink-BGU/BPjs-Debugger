package il.ac.bgu.se.bp.debugger.manage;

import il.ac.bgu.se.bp.debugger.BPJsDebugger;

public interface DebuggerFactory<T> {
    BPJsDebugger<T> getBPJsDebugger(String debuggerId, String filename);
}
