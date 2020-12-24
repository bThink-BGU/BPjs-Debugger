package il.ac.bgu.se.bp;

import java.util.Objects;

public class DummyDataRequest {
    private String code;

    public DummyDataRequest() {
    }

    public DummyDataRequest(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DummyDataRequest that = (DummyDataRequest) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return "DummyDataRequest{" +
                "code='" + code + '\'' +
                '}';
    }
}
