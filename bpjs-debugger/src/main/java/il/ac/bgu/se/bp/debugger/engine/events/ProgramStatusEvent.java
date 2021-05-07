package il.ac.bgu.se.bp.debugger.engine.events;

import il.ac.bgu.se.bp.socket.exit.ProgramStatus;
import il.ac.bgu.se.bp.utils.observer.BPEvent;
import il.ac.bgu.se.bp.utils.visitor.PublisherVisitor;

public class ProgramStatusEvent extends BPEvent<ProgramStatus> {

    public ProgramStatusEvent(String debuggerId, boolean isRunning) {
        super(debuggerId, new ProgramStatus(isRunning));
    }

    @Override
    public void accept(PublisherVisitor visitor) {
        visitor.visit(debuggerId, event);
    }

    @Override
    public String getEventType() {
        return "ProgramExit";
    }
}
