package il.ac.bgu.se.bp.debugger.state;

public class EventInfo {
    private String name;

    public EventInfo(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
