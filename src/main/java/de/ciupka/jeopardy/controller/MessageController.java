package de.ciupka.jeopardy.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import de.ciupka.jeopardy.configs.UserPrincipal;
import de.ciupka.jeopardy.controller.messages.BoardUpdate;
import de.ciupka.jeopardy.controller.messages.QuestionIdentifier;
import de.ciupka.jeopardy.controller.messages.SelectedQuestion;
import de.ciupka.jeopardy.game.Category;
import de.ciupka.jeopardy.game.GameService;
import de.ciupka.jeopardy.game.Player;
import de.ciupka.jeopardy.game.questions.AbstractQuestion;
import de.ciupka.jeopardy.services.NotificationService;

/**
 * MessageController is the {@code Controller} for WebSocket messages.
 * 
 * @author Alexander Ciupka
 */
@Controller
public class MessageController {

    @Autowired
    private GameService game;

    @Autowired
    private NotificationService notifications;

    @MessageMapping("/on-connect")
    public void onConnect(Principal principal) {
        UserPrincipal up = (UserPrincipal) principal;

        this.notifications.sendGameMasterUpdate(up.getName());
        this.notifications.sendBoardUpdate(up.getName());
        this.notifications.sendLobbyUpdate(up.getName());
        this.notifications.sendQuestionUpdate(up.getName());
    }

    /**
     * Websocket Message Handler for join requests that will add the requesting
     * {@code Player} to the lobby and send a lobby update to all other connected
     * players. For furthe information on the adding {@code Player} to the
     * {@code GameService} see {@link de.ciupka.jeopardy.game.GameService#addPlayer
     * GameService.addPlayer}
     * 
     * @param name      The name of the {@code Player} that wants to join.
     * @param principal The {@code de.ciupka.jeopardy.configs.UserPrincipal} that
     *                  was created when a WebSocket connection was established.
     * @return True if a player was added to the lobby. If a player already exists
     *         with {@code name}, it will return false.
     */
    @MessageMapping("/join")
    @SendToUser("/topic/join")
    public boolean join(String name, Principal principal) {
        UserPrincipal up = (UserPrincipal) principal;
        final boolean added = this.game.addPlayer(up.getID(), name);

        /**
         * TODO: Might be necessary to send more data on lobby updates.
         * In general it might be smart to also send the 'reason' for a lobby update
         * with extra data based on reasoning.
         */
        notifications.sendLobbyUpdate(null);
        notifications.sendBoardUpdate(up.getName());

        return added;
    }

    @MessageMapping("/buzzer")
    @SendTo("/topic/buzzer-state")
    public boolean buzzer(Principal principal) {
        UserPrincipal up = (UserPrincipal) principal;

        return false;
    }

    @MessageMapping("/question")
    @SendToUser("/topic/question")
    public String question(QuestionIdentifier identifier, Principal principal) {
        UserPrincipal up = (UserPrincipal) principal;
        Player active = this.game.getCurrentPlayer();

        if (!active.getUuid().equals(up.getID())) {
            return "Du bist nicht an der Reihe!";
        }

        if (this.game.getCurrentQuestionIdentifier() != null) {
            return "Es gibt bereits eine Frage!";
        }

        Category cat = this.game.getCategory(identifier.getCategory());
        if (cat == null) {
            return "Ungültige Kategorie-Auswahl";
        }
        AbstractQuestion qst = cat.getQuestion(identifier.getQuestion());
        if (qst == null) {
            return "Ungültige Fragen-Auswahl";
        }

        if (qst.isAnswered()) {
            return "Frage wurde bereits beantwortet";
        }

        this.game.setCurrentQuestionIdentifier(identifier);

        this.notifications.sendBoardUpdate(null);
        this.notifications.sendQuestionUpdate(null);
        return null;
    }

    @MessageMapping("/gamemaster")
    @SendToUser("topic/gamemaster")
    public boolean setGameMaster(Principal principal) {
        UserPrincipal up = (UserPrincipal) principal;
        this.game.setMaster(up.getID());

        this.notifications.sendGameMasterUpdate(null);
    
        return true;
    }

    @MessageMapping("/skip-question")
    public void skipQuestion(Principal principal) {
        UserPrincipal up = (UserPrincipal) principal;

        Player p = this.game.getPlayerByID(up.getID());

        game.answerQuestion(p, false);

        this.notifications.sendQuestionUpdate(null);
        this.notifications.sendLobbyUpdate(null);
        this.notifications.sendBoardUpdate(null);
    }
}
