package de.ciupka.jeopardy.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class WebSocketErrorAdvice {

    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebSocketErrorAdvice(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageExceptionHandler
    public void handleExceptions(Throwable ex, UserPrincipal user) {
        messagingTemplate.convertAndSendToUser(
                user.getName(),
                "/queue/errors",
                ex.getMessage());
    }
}
