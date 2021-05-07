package il.ac.bgu.se.bp.socket.exit;

import java.io.Serializable;
import java.util.Objects;

public class ProgramStatus implements Serializable {
    private static final long serialVersionUID = -1206523140785645037L;

    private boolean isRunning;

    public ProgramStatus(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProgramStatus that = (ProgramStatus) o;
        return isRunning == that.isRunning;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isRunning);
    }

    @Override
    public String toString() {
        return "ProgramStatus{" +
                "isRunning=" + isRunning +
                '}';
    }
}
