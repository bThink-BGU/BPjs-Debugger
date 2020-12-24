package il.ac.bgu.se.bp;

import java.util.Objects;

public class DummyDataResponse {
    private String msg;

    public DummyDataResponse(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DummyDataResponse that = (DummyDataResponse) o;
        return Objects.equals(msg, that.msg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(msg);
    }

    @Override
    public String toString() {
        return "DummyDataResponse{" +
                "msg='" + msg + '\'' +
                '}';
    }
}
