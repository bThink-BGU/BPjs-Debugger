package il.ac.bgu.se.bp.debugger.state;

import java.util.List;

public class BPDebuggerState {
    List<BThreadInfo> bThreadInfoList;
    EventsStatus eventsStatus;
    public BPDebuggerState(List<BThreadInfo> bThreadInfoList, EventsStatus eventsStatus) {
        this.bThreadInfoList = bThreadInfoList;
        this.eventsStatus = eventsStatus;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("BPDebuggerState\n");
        for(BThreadInfo b : bThreadInfoList)
            s.append("["+ b.toString()+"],\n\n");
        s.append(eventsStatus.toString()+ "\n");
        return s.toString();
    }
}
