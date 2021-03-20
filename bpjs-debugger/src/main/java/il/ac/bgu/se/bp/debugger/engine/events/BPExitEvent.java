package il.ac.bgu.se.bp.debugger.engine.events;

import il.ac.bgu.se.bp.socket.exit.ProgramExit;
import il.ac.bgu.se.bp.utils.observer.BPEvent;
import il.ac.bgu.se.bp.utils.visitor.PublisherVisitor;

public class BPExitEvent extends BPEvent<ProgramExit> {

    public BPExitEvent(String debuggerId) {
        super(debuggerId, new ProgramExit());
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
