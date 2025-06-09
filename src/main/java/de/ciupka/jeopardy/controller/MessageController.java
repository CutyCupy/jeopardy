package de.ciupka.jeopardy.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
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

    /**
     * Websocket Message Handler for join requests that will add the requesting
     * {@code Player} to the lobby and send a lobby update to all other connected
     * players. For furthe information on the adding {@code Player} to the
     * {@code LobbyService} see {@link de.ciupka.jeopardy.game.LobbyService#addPlayer LobbyService.addPlayer}
     * 
     * @param name      The name of the {@code Player} that wants to join.
     * @param principal The {@code de.ciupka.jeopardy.configs.UserPrincipal} that
     *                  was created when a WebSocket connection was established.
     * @return True if a player was added to the lobby. If a player already exists
     *         with {@code name}, it will return false.
     * @throws Exception
     */
    @MessageMapping("/join")
    @SendToUser("/topic/join")
    public boolean join(String name, Principal principal) throws Exception {
        final boolean added = this.lobby.addPlayer(((UserPrincipal) principal).getID(), name);

        /**
         * TODO: Might be necessary to send more data on lobby updates.
         * In general it might be smart to also send the 'reason' for a lobby update
         * with extra data based on reasoning.
         */
        notifications.broadcastMessage(LOBBY_UPDATE, this.lobby.getPlayers());

        return added;
    }

}
