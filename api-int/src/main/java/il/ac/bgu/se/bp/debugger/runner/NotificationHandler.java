package il.ac.bgu.se.bp.debugger.runner;


import java.io.Serializable;

public interface NotificationHandler {
    void sendNotification(String userId, Serializable json);
}
