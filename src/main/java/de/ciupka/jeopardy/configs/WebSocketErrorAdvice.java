package de.ciupka.jeopardy.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.ControllerAdvice;

import de.ciupka.jeopardy.controller.messages.Notification;
import de.ciupka.jeopardy.controller.messages.Notification.NotificationType;
import de.ciupka.jeopardy.services.NotificationService;

@ControllerAdvice
public class WebSocketErrorAdvice {

    @Autowired
    private NotificationService notifications;

    public WebSocketErrorAdvice() {
    }

    @MessageExceptionHandler
    public void handleExceptions(Throwable ex, UserPrincipal user) {
        this.notifications.sendNotification(new Notification(NotificationType.DANGER, ex.getMessage()), user.getID());
    }
}
