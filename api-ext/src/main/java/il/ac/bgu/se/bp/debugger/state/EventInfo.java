package il.ac.bgu.se.bp.debugger.state;

import java.io.Serializable;
import java.util.Objects;

public class EventInfo implements Serializable {
    private static final long serialVersionUID = 983874658293202351L;

    private String name;

    public EventInfo() {
        this.name = "";
    }

    public EventInfo(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventInfo eventInfo = (EventInfo) o;
        return name.equals(eventInfo.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
