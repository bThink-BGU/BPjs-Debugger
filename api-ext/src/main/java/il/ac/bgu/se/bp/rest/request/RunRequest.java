package il.ac.bgu.se.bp.rest.request;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class RunRequest implements Serializable {
    private static final long serialVersionUID = 4758855571059683558L;

    protected String sourceCode;
    protected UUID socketId;

    public RunRequest() {
    }

    public RunRequest(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public UUID getSocketId() {
        return socketId;
    }

    public void setSocketId(UUID socketId) {
        this.socketId = socketId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RunRequest that = (RunRequest) o;
        return Objects.equals(sourceCode, that.sourceCode) && Objects.equals(socketId, that.socketId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceCode, socketId);
    }

    @Override
    public String toString() {
        return "RunRequest{" +
                "sourceCode='" + sourceCode + '\'' +
                ", socketId=" + socketId +
                '}';
    }
}
