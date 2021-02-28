package il.ac.bgu.se.bp.debugger.engine;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;

import java.util.List;
import java.util.Map;

public class BThreadInfo {
    String name;
    Map<Integer, Map<String, String>> env;
    EventSet wait;
    EventSet blocked;
    List<BEvent> requested;
    public BThreadInfo(String name, Map<Integer, Map<String, String>> env, EventSet wait, EventSet blocked, List<BEvent> requested) {
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
