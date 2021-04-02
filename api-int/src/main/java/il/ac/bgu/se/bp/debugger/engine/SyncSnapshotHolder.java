package il.ac.bgu.se.bp.debugger.engine;

import il.ac.bgu.se.bp.utils.Pair;

import java.util.HashMap;
import java.util.SortedMap;

public interface SyncSnapshotHolder<T, U> {
    void addSyncSnapshot(T snapshot, U event);
    T popKey(long snapshotTime);
    SortedMap<Long, Pair<T, U>> getAllSyncSnapshots();
    HashMap<Long,U> getEventsHistoryStack(int from, int to);
}
