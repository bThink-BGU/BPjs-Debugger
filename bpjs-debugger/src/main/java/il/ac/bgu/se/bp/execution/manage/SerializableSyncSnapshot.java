package il.ac.bgu.se.bp.execution.manage;

import il.ac.bgu.cs.bp.bpjs.model.*;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SerializableSyncSnapshot implements Serializable {
    private static final long serialVersionUID = 5223650425034528468L;

    private Set<BThreadSyncSnapshot> threadSnapshots;
    private List<BEvent> externalEvents;
    private FailedAssertion violationRecord;
    private BProgram bProgram;

    public SerializableSyncSnapshot() {
    }

    public SerializableSyncSnapshot(BProgramSyncSnapshot syncSnapshot) {
        threadSnapshots = syncSnapshot.getBThreadSnapshots();
        externalEvents = syncSnapshot.getExternalEvents();
        violationRecord = syncSnapshot.getFailedAssertion();
        bProgram = syncSnapshot.getBProgram();
    }

    public Set<BThreadSyncSnapshot> getThreadSnapshots() {
        return threadSnapshots;
    }

    public List<BEvent> getExternalEvents() {
        return externalEvents;
    }

    public FailedAssertion getViolationRecord() {
        return violationRecord;
    }

    @Override
    public boolean equals(Object queryType) {
        if (this == queryType) {
            return true;
        }
        if (queryType == null || getClass() != queryType.getClass()) {
            return false;
        }
        SerializableSyncSnapshot that = (SerializableSyncSnapshot) queryType;
        return Objects.equals(threadSnapshots, that.threadSnapshots) &&
                Objects.equals(externalEvents, that.externalEvents) &&
                Objects.equals(violationRecord, that.violationRecord);
    }

    @Override
    public int hashCode() {
        return Objects.hash(threadSnapshots, externalEvents, violationRecord);
    }

    @Override
    public String toString() {
        return "SerializableSyncSnapshot{" +
                "threadSnapshots=" + threadSnapshots +
                ", externalEvents=" + externalEvents +
                ", violationRecord=" + violationRecord +
                '}';
    }
}
