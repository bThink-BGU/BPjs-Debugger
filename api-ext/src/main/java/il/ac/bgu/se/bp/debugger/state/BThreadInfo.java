package il.ac.bgu.se.bp.debugger.state;
import java.io.Serializable;
import java.util.*;

public class BThreadInfo implements Serializable {
    private static final long serialVersionUID = 2208145820539894522L;

    private String name;
    private Map<Integer, Map<String, String>> env;
    private EventInfo wait;
    private EventInfo blocked;
    private Set<EventInfo> requested;

    public BThreadInfo() {
    }

    public BThreadInfo(String name, Map<Integer, Map<String, String>> env) {
        this.name = name;
        this.env = env;
        this.wait = new EventInfo();
        this.blocked = new EventInfo();
        this.requested = new HashSet<>();
    }

    public BThreadInfo(String name, Map<Integer, Map<String, String>> env, EventInfo wait, EventInfo blocked, Set<EventInfo> requested) {
        this.name = name;
        this.env = env == null ? new HashMap<>() : env;
        this.wait = wait;
        this.blocked= blocked;
        this.requested = requested;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Integer, Map<String, String>> getEnv() {
        return env;
    }

    public void setEnv(Map<Integer, Map<String, String>> env) {
        this.env = env;
    }

    public EventInfo getWait() {
        return wait;
    }

    public void setWait(EventInfo wait) {
        this.wait = wait;
    }

    public EventInfo getBlocked() {
        return blocked;
    }

    public void setBlocked(EventInfo blocked) {
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
        BThreadInfo that = (BThreadInfo) o;
        return name.equals(that.name) &&
                env.equals(that.env) &&
                Objects.equals(wait,that.wait) &&
                Objects.equals(blocked,that.blocked) &&
                Objects.equals(requested,that.requested);
    }

    @Override
    public String toString() {
        StringBuilder envS = new StringBuilder();
        for (Map.Entry e : env.entrySet()) {
            envS.append(e.getKey()+ ":"+ e.getValue()).append("\n");
        }
        return "BThreadInfo{" +
                "name='" + name + '\'' +
                ", env=" + envS +
                ", wait=" + wait +
                ", blocked=" + blocked +
                ", requested=" + requested +
                '}';
    }
}
