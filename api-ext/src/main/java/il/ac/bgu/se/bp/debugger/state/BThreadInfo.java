package il.ac.bgu.se.bp.debugger.state;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BThreadInfo {
    private String name;
    private Map<Integer, Map<String, String>> env;
    private EventInfo wait;
    private EventInfo blocked;
    private Set<EventInfo> requested;

    public BThreadInfo() {
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
