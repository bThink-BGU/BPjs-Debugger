package il.ac.bgu.se.bp.debugger.engine;

import il.ac.bgu.cs.bp.bpjs.model.*;
import il.ac.bgu.se.bp.utils.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class SyncSnapshotHolderImpl implements SyncSnapshotHolder<BProgramSyncSnapshot, BEvent> {

    private SortedMap<Long, Pair<BProgramSyncSnapshot, BEvent>> snapshotsByTimeChosen = new TreeMap<>();

    @Override
    public synchronized void addSyncSnapshot(BProgramSyncSnapshot snapshot, BEvent event) {
        if (snapshot == null)
            return;

        long currentTime = System.currentTimeMillis();
        snapshotsByTimeChosen.put(currentTime, Pair.of(snapshot, event));
    }

    @Override
    public synchronized BProgramSyncSnapshot popKey(long snapshotTime) {
        if (!snapshotsByTimeChosen.containsKey(snapshotTime)) {
            return null;
        }

        BProgramSyncSnapshot oldBProgramSyncSnapshot = snapshotsByTimeChosen.get(snapshotTime).getLeft();
        snapshotsByTimeChosen = cloneTreeMap(snapshotsByTimeChosen.headMap(snapshotTime));

        return cloneBProgramSyncSnapshot(oldBProgramSyncSnapshot);
    }

    private BProgramSyncSnapshot cloneBProgramSyncSnapshot(BProgramSyncSnapshot oldBProgramSyncSnapshot) {
        BProgram aBProgram = oldBProgramSyncSnapshot.getBProgram();
        Set<BThreadSyncSnapshot> someThreadSnapshots = oldBProgramSyncSnapshot.getBThreadSnapshots();
        List<BEvent> someExternalEvents = oldBProgramSyncSnapshot.getExternalEvents();
        FailedAssertion aViolationRecord = oldBProgramSyncSnapshot.getFailedAssertion();
        return new BProgramSyncSnapshot(aBProgram, someThreadSnapshots, someExternalEvents, aViolationRecord);
    }

    @Override
    public SortedMap<Long, Pair<BProgramSyncSnapshot, BEvent>> getAllSyncSnapshots() {
        return cloneTreeMap(snapshotsByTimeChosen);
    }

    @Override
    public List<BEvent> getEventsHistoryStack(int from, int to) {
        List<BEvent> eventsHistory = snapshotsByTimeChosen.values().stream().map(Pair::getRight).filter(Objects::nonNull).collect(Collectors.toList());
        Collections.reverse(eventsHistory);
        if(from == -1 && to == -1)
            return eventsHistory;
        if(from > eventsHistory.size() || (to > from))
            return new LinkedList<>();
        int endIdx = to > eventsHistory.size()? eventsHistory.size(): to;
        return eventsHistory.subList(from,endIdx);
    }

    private TreeMap<Long, Pair<BProgramSyncSnapshot, BEvent>> cloneTreeMap(SortedMap<Long, Pair<BProgramSyncSnapshot, BEvent>> treeMap) {
        return new TreeMap<>(treeMap);
    }
}
