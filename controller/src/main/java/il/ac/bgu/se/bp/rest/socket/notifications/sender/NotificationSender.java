package il.ac.bgu.se.bp.rest.socket.notifications.sender;

import java.io.Serializable;

public interface NotificationSender {
    void sendNotification(String userId, String updateURI, Serializable json);
}
