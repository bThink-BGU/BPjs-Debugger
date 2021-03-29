package il.ac.bgu.se.bp.service.manage;

import java.time.LocalDateTime;
import java.util.Objects;

public class UserSession {

    private String sessionId;
    private String userId;
    private LocalDateTime lastOperationTime;

    public UserSession(String sessionId, String userId, LocalDateTime lastOperationTime) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.lastOperationTime = lastOperationTime;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getLastOperationTime() {
        return lastOperationTime;
    }

    public void setLastOperationTime(LocalDateTime lastOperationTime) {
        this.lastOperationTime = lastOperationTime;
    }

    public <T> UserProgramSession<T> withProgram(T program) {
        UserProgramSession<T> newUserSession = new UserProgramSession<>(sessionId, userId, lastOperationTime);
        newUserSession.setProgram(program);
        return newUserSession;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserSession that = (UserSession) o;
        return Objects.equals(sessionId, that.sessionId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(lastOperationTime, that.lastOperationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, userId, lastOperationTime);
    }

    @Override
    public String toString() {
        return "UserSession{" +
                "sessionId='" + sessionId + '\'' +
                ", userId='" + userId + '\'' +
                ", lastOperationTime=" + lastOperationTime +
                '}';
    }
}
