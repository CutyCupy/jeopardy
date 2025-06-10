package de.ciupka.jeopardy.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * This service provides utility methods for communication with all or specific WebSocket clients.
 * @author Alexander Ciupka
 */
@Service
public class NotificationService {
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastMessage(String topic, Object message) {
        messagingTemplate.convertAndSend(topic, message);
    }

    public void privateMessage(final String userId, String topic, Object message) {
        messagingTemplate.convertAndSendToUser(userId, topic, message);
    }
}