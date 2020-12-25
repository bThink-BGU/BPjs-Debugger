package il.ac.bgu.se.bp;

import java.util.Objects;

public class ExecuteBPjsResponse {
    private String userId;

    public ExecuteBPjsResponse(String msg) {
        this.userId = msg;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExecuteBPjsResponse that = (ExecuteBPjsResponse) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "ExecuteBPjsResponse{" +
                "userId='" + userId + '\'' +
                '}';
    }
}
