package il.ac.bgu.se.bp.rest.request;

import java.io.Serializable;
import java.util.Objects;

public class RunRequest implements Serializable {
    private static final long serialVersionUID = 4758855571059683558L;

    protected String sourceCode;

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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RunRequest that = (RunRequest) o;
        return Objects.equals(sourceCode, that.sourceCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceCode);
    }

    @Override
    public String toString() {
        return "RunRequest{" +
                "sourceCode='" + sourceCode + '\'' +
                '}';
    }
}
