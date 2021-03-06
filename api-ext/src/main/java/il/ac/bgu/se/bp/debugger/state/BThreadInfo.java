package il.ac.bgu.se.bp.debugger.state;
import java.util.Map;
import java.util.Set;

public class BThreadInfo {
    String name;
    Map<Integer, Map<String, String>> env;
    EventInfo wait;
    EventInfo blocked;
    Set<EventInfo> requested;

    public BThreadInfo(String name, Map<Integer, Map<String, String>> env, EventInfo wait, EventInfo blocked, Set<EventInfo> requested) {
        this.name = name;
        this.env = env;
        this.wait = wait;
        this.blocked= blocked;
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
