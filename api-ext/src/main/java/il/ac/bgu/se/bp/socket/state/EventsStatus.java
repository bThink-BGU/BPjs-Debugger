package il.ac.bgu.se.bp.socket.state;

import java.io.Serializable;
import java.util.*;

public class EventsStatus implements Serializable {
    private static final long serialVersionUID = -2004676986407911544L;

    private List<EventInfo> wait;
    private List<EventInfo> blocked;
    private Set<EventInfo> requested;

    public EventsStatus() {
        this.wait = new ArrayList<>();
        this.blocked = new ArrayList<>();
        this.requested = new HashSet<>();
    }

    public EventsStatus(List<EventInfo> wait, List<EventInfo> blocked, Set<EventInfo> requested) {
        this.wait = wait;
        this.blocked = blocked;
        this.requested = requested;
    }

    public List<EventInfo> getWait() {
        return wait;
    }

    public void setWait(List<EventInfo> wait) {
        this.wait = wait;
    }

    public List<EventInfo> getBlocked() {
        return blocked;
    }

    public void setBlocked(List<EventInfo> blocked) {
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
        return wait.containsAll(that.wait) && that.wait.containsAll(wait) &&
                blocked.containsAll(that.blocked) && that.blocked.containsAll(blocked) &&
                requested.equals(that.requested);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wait, blocked, requested);
    }

    public String prettier(String ... prefix) {
        String pref = prefix != null && prefix.length > 0 ? prefix[0] : "";
        return pref + "EventsStatus{\n" +
                pref + "\twait=" + wait + ",\n" +
                pref + "\tblocked=" + blocked + ",\n" +
                pref + "\trequested=" + requested + ",\n" +
                pref + '}';
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
