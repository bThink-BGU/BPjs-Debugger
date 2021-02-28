package il.ac.bgu.se.bp.debugger.engine;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.SyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EventsStatus {
    List<EventSet> wait;
    List<EventSet> blocked;
    List<BEvent> requested;

    public EventsStatus(Set<SyncStatement> statements) {
        wait = statements.stream().map(SyncStatement::getWaitFor).collect(Collectors.toList());
        blocked = statements.stream().map(SyncStatement::getBlock).collect(Collectors.toList());
        requested = statements.stream().map(SyncStatement::getRequest).flatMap(Collection::stream).collect(Collectors.toList());
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
