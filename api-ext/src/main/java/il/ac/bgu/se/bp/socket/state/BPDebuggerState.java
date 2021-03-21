package il.ac.bgu.se.bp.socket.state;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class BPDebuggerState implements Serializable {
    private static final long serialVersionUID = 6320377753998745711L;

    private List<BThreadInfo> bThreadInfoList;
    private EventsStatus eventsStatus;
    private EventInfo chosenEvent;
    private String currentRunningBT;
    private Integer currentLineNumber;


    public BPDebuggerState() {
        this.bThreadInfoList = new ArrayList<>();
        this.eventsStatus = new EventsStatus(new ArrayList<>(), new ArrayList<>(), new HashSet<>());
    }

    public BPDebuggerState(List<BThreadInfo> bThreadInfoList, EventsStatus eventsStatus) {
        this.bThreadInfoList = bThreadInfoList;
        this.eventsStatus = eventsStatus;
    }

    public BPDebuggerState(List<BThreadInfo> bThreadInfoList, EventsStatus eventsStatus, EventInfo chosenEvent) {
        this.bThreadInfoList = bThreadInfoList;
        this.eventsStatus = eventsStatus;
        this.chosenEvent = chosenEvent;
    }

    public BPDebuggerState(List<BThreadInfo> bThreadInfoList, EventsStatus eventsStatus, EventInfo chosenEvent, String currentRunningBT, Integer currentLineNumber) {
        this.bThreadInfoList = bThreadInfoList;
        this.eventsStatus = eventsStatus;
        this.chosenEvent = chosenEvent;
        this.currentRunningBT = currentRunningBT;
        this.currentLineNumber = currentLineNumber;
    }

    public String getCurrentRunningBT() {
        return currentRunningBT;
    }

    public void setCurrentRunningBT(String currentRunningBT) {
        this.currentRunningBT = currentRunningBT;
    }

    public Integer getCurrentLineNumber() {
        return currentLineNumber;
    }

    public void setCurrentLineNumber(Integer currentLineNumber) {
        this.currentLineNumber = currentLineNumber;
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BPDebuggerState that = (BPDebuggerState) o;
        return bThreadInfoList.containsAll(that.bThreadInfoList) && that.bThreadInfoList.containsAll(bThreadInfoList) &&
                Objects.equals(eventsStatus, that.eventsStatus) &&
                Objects.equals(chosenEvent, that.chosenEvent) &&
                Objects.equals(currentRunningBT, that.currentRunningBT) &&
                Objects.equals(currentLineNumber, that.currentLineNumber);
    }

    public String prettier() {
        StringBuilder s = new StringBuilder();
        s.append("\n{\n")
                .append("BPDebuggerState\n");

        bThreadInfoList.forEach(bThreadInfo -> s.append(bThreadInfo.prettier("\t")).append("\n\n"));

        s.append(eventsStatus.prettier("\t"))
                .append("\n")
                .append("\tcurrentRunningBT: ").append(currentRunningBT)
                .append("\n")
                .append("\tcurrentLineNumber: ").append(currentLineNumber)
                .append("\n")
                .append("}");

        return s.toString();
    }

    @Override
    public String toString() {
        return "BPDebuggerState{" +
                "bThreadInfoList=" + bThreadInfoList +
                ", eventsStatus=" + eventsStatus +
                ", chosenEvent=" + chosenEvent +
                ", currentRunningBT='" + currentRunningBT + '\'' +
                ", currentLineNumber=" + currentLineNumber +
                '}';
    }
}
