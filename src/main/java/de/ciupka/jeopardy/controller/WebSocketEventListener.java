package de.ciupka.jeopardy.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import de.ciupka.jeopardy.configs.UserPrincipal;
import de.ciupka.jeopardy.game.GameService;
import de.ciupka.jeopardy.services.NotificationService;

@Component
public class WebSocketEventListener {

    @Autowired
    private GameService game;

    @Autowired
    private NotificationService notifications;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headerAccessor.getUser();

        if (user == null || !(user instanceof UserPrincipal)) {
            return;
        }

        UserPrincipal up = (UserPrincipal) user;

        if (game.onDisconnect(up.getID())) {
            notifications.sendLobbyUpdate(null);
            notifications.sendGameMasterUpdate(null);
        }
    }
}