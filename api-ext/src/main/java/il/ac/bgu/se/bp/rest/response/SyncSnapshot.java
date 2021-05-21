package il.ac.bgu.se.bp.rest.response;

import java.io.Serializable;
import java.util.Objects;

public class SyncSnapshot implements Serializable {
    private static final long serialVersionUID = 8433468883477207141L;

    private String sourceCode;
    private Serializable syncSnapshot;

    public SyncSnapshot() {
    }

    public SyncSnapshot(String sourceCode, Serializable syncSnapshot) {
        this.sourceCode = sourceCode;
        this.syncSnapshot = syncSnapshot;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public Serializable getSyncSnapshot() {
        return syncSnapshot;
    }

    public void setSyncSnapshot(Serializable syncSnapshot) {
        this.syncSnapshot = syncSnapshot;
    }

    @Override
    public boolean equals(Object queryType) {
        if (this == queryType) {
            return true;
        }
        if (queryType == null || getClass() != queryType.getClass()) {
            return false;
        }
        SyncSnapshot that = (SyncSnapshot) queryType;
        return Objects.equals(sourceCode, that.sourceCode) &&
                Objects.equals(syncSnapshot, that.syncSnapshot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceCode, syncSnapshot);
    }

    @Override
    public String toString() {
        return "SyncSnapshot{" +
                "sourceCode='" + sourceCode + '\'' +
                ", syncSnapshot=" + syncSnapshot +
                '}';
    }
}
