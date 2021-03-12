package il.ac.bgu.se.bp.debugger.state;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class EventsStatus implements Serializable {
    private static final long serialVersionUID = -2004676986407911544L;

    private Set<EventInfo> wait;
    private Set<EventInfo> blocked;
    private Set<EventInfo> requested;

    public EventsStatus() {
        this.wait = new HashSet<>();
        this.blocked = new HashSet<>();
        this.requested = new HashSet<>();
    }

    public EventsStatus(Set<EventInfo> wait, Set<EventInfo> blocked, Set<EventInfo> requested) {
        this.wait = wait;
        this.blocked = blocked;
        this.requested = requested;
    }

    public Set<EventInfo> getWait() {
        return wait;
    }

    public void setWait(Set<EventInfo> wait) {
        this.wait = wait;
    }

    public Set<EventInfo> getBlocked() {
        return blocked;
    }

    public void setBlocked(Set<EventInfo> blocked) {
        this.blocked = blocked;
    }

    public Set<EventInfo> getRequested() {
        return requested;
    }

    public void setRequested(Set<EventInfo> requested) {
        this.requested = requested;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventsStatus that = (EventsStatus) o;
        return wait.equals(that.wait) &&
                blocked.equals(that.blocked) &&
                requested.equals(that.requested);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wait, blocked, requested);
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
