package il.ac.bgu.se.bp.debugger.state;

import java.util.Set;

public class EventsStatus {
    Set<EventInfo> wait;
    Set<EventInfo> blocked;
    Set<EventInfo> requested;

    public EventsStatus(Set<EventInfo> wait, Set<EventInfo> blocked, Set<EventInfo> requested) {
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
