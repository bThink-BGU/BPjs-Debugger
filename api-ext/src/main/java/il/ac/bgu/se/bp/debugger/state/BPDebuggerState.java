package il.ac.bgu.se.bp.debugger.state;

import java.util.List;

public class BPDebuggerState {
    private List<BThreadInfo> bThreadInfoList;
    private EventsStatus eventsStatus;
    private EventInfo chosenEvent;

    public BPDebuggerState(List<BThreadInfo> bThreadInfoList, EventsStatus eventsStatus) {
        this.bThreadInfoList = bThreadInfoList;
        this.eventsStatus = eventsStatus;
    }

    public List<BThreadInfo> getbThreadInfoList() {
        return bThreadInfoList;
    }

    public void setbThreadInfoList(List<BThreadInfo> bThreadInfoList) {
        this.bThreadInfoList = bThreadInfoList;
    }

    public EventsStatus getEventsStatus() {
        return eventsStatus;
    }

    public void setEventsStatus(EventsStatus eventsStatus) {
        this.eventsStatus = eventsStatus;
    }

    public EventInfo getChosenEvent() {
        return chosenEvent;
    }

    public void setChosenEvent(EventInfo chosenEvent) {
        this.chosenEvent = chosenEvent;
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
