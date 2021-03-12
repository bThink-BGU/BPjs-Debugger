package il.ac.bgu.se.bp.rest.request;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class DebugRequest extends RunRequest {
    private static final long serialVersionUID = 6588719020597509990L;

    private List<Integer> breakpoints = new LinkedList<>();
    private boolean stopOnBreakpointsToggle;
    private boolean stopOnSyncStateToggle;

    public DebugRequest() {
    }

    public DebugRequest(String code, List<Integer> breakpoints) {
        this.sourceCode = code;
        this.breakpoints = breakpoints;
    }

    public DebugRequest(String code) {
        this.sourceCode = code;
    }

    public List<Integer> getBreakpoints() {
        return breakpoints;
    }

    public void setBreakpoints(List<Integer> breakpoints) {
        this.breakpoints = breakpoints;
    }

    public boolean isStopOnBreakpointsToggle() {
        return stopOnBreakpointsToggle;
    }

    public void setStopOnBreakpointsToggle(boolean stopOnBreakpointsToggle) {
        this.stopOnBreakpointsToggle = stopOnBreakpointsToggle;
    }

    public boolean isStopOnSyncStateToggle() {
        return stopOnSyncStateToggle;
    }

    public void setStopOnSyncStateToggle(boolean stopOnSyncStateToggle) {
        this.stopOnSyncStateToggle = stopOnSyncStateToggle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DebugRequest that = (DebugRequest) o;
        return stopOnBreakpointsToggle == that.stopOnBreakpointsToggle && stopOnSyncStateToggle == that.stopOnSyncStateToggle && Objects.equals(breakpoints, that.breakpoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), breakpoints, stopOnBreakpointsToggle, stopOnSyncStateToggle);
    }

    @Override
    public String toString() {
        return "DebugRequest{" +
                "breakpoints=" + breakpoints +
                ", stopOnBreakpointsToggle=" + stopOnBreakpointsToggle +
                ", stopOnSyncStateToggle=" + stopOnSyncStateToggle +
                ", sourceCode='" + sourceCode + '\'' +
                ", socketId=" + socketId +
                '}';
    }
}
