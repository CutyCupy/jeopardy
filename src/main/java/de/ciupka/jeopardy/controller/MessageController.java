package de.ciupka.jeopardy.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import de.ciupka.jeopardy.configs.UserPrincipal;
import de.ciupka.jeopardy.game.LobbyService;
import de.ciupka.jeopardy.services.NotificationService;

@Controller
public class MessageController {

    private static final String LOBBY_UPDATE = "/topic/lobby-update";

    @Autowired
    private LobbyService lobby;

    @Autowired
    private NotificationService notifications;

    @MessageMapping("/join")
    public void join(String message, Principal principal) throws Exception {
        this.lobby.addPlayer(((UserPrincipal) principal).getID(), message);

        notifications.broadcastMessage(LOBBY_UPDATE, this.lobby.getPlayers());
    }

}
