package il.ac.bgu.se.bp;

import java.util.Arrays;
import java.util.Objects;

public class DebugRequest {
    private String code;
    private int[] breakpoints;
    private boolean stopOnBreakpointsToggle;
    private boolean stopOnSyncStateToggle;

    public DebugRequest() {
    }

    public DebugRequest(String code, int[] breakpoints) {
        this.code = code;
        this.breakpoints = breakpoints;
    }

    public DebugRequest(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int[] getBreakpoints() {
        return breakpoints;
    }

    public void setBreakpoints(int[] breakpoints) {
        this.breakpoints = breakpoints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DebugRequest that = (DebugRequest) o;
        return Objects.equals(code, that.code) && Arrays.equals(breakpoints, that.breakpoints);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(code);
        result = 31 * result + Arrays.hashCode(breakpoints);
        return result;
    }

    @Override
    public String toString() {
        return "DebugRequest{" +
                "code='" + code + '\'' +
                ", breakpoints=" + Arrays.toString(breakpoints) +
                '}';
    }
}
