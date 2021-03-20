package il.ac.bgu.se.bp.rest.socket.notifications.handler;

import il.ac.bgu.se.bp.debugger.runner.NotificationHandler;
import il.ac.bgu.se.bp.rest.socket.notifications.sender.NotificationSender;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;

public abstract class AbstractNotificationHandler implements NotificationHandler {

    @Autowired
    private NotificationSender notificationSender;

    protected abstract String getUpdateURI();

    public void sendNotification(String userId, Serializable json) {
        notificationSender.sendNotification(userId, getUpdateURI(), json);
    }
}
