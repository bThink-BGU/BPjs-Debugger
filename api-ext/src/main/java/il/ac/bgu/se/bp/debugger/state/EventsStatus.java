package il.ac.bgu.se.bp.debugger.state;

import java.util.List;

public class EventsStatus {
    List<String> wait;
    List<String> blocked;
    List<String> requested;

    public EventsStatus(List<String> wait, List<String> blocked, List<String> requested) {
        this.wait = wait;
        this.blocked = blocked;
        this.requested = requested;
    }

    @Override
    public String toString() {
        return "EventsStatus{" +
                "wait=" + wait +
                ", blocked=" + blocked +
                ", requested=" + requested +
                '}';
    }
}
