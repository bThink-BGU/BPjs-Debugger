package il.ac.bgu.se.bp.service.notification;


import java.io.Serializable;

public interface NotificationHandler {
    void sendNotification(String userId, Serializable json);
}
