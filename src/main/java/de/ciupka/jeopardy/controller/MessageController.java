package de.ciupka.jeopardy.controller;

import java.security.Principal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import de.ciupka.jeopardy.configs.UserPrincipal;
import de.ciupka.jeopardy.controller.messages.Answer;
import de.ciupka.jeopardy.controller.messages.AnswerEvaluation;
import de.ciupka.jeopardy.controller.messages.QuestionIdentifier;
import de.ciupka.jeopardy.game.GameService;
import de.ciupka.jeopardy.game.Player;
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
        this.notifications.sendLobbyUpdate(up.getName());

        if (this.game.isActive()) {
            this.notifications.sendBoardUpdate(up.getName());
            this.notifications.sendQuestionUpdate(up.getName());
        }
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
        if (this.game.isActive()) {
            notifications.sendBoardUpdate(up.getName());
        }

        return added;
    }

    @MessageMapping("/start-game")
    public void startGame(Principal principal) {
        UserPrincipal up = (UserPrincipal) principal;

        if (!up.getID().equals(this.game.getMaster()) || this.game.isActive()) {
            return;
        }

        this.game.start();

        this.notifications.sendBoardUpdate(null);
    }

    @MessageMapping("/submit-answer")
    public boolean submitAnswer(String answer, Principal principal) {
        UserPrincipal up = (UserPrincipal) principal;
        this.notifications.setBuzzer(up.getName(), false);

        Player answering = this.game.getPlayerByID(up.getID());

        UUID master = this.game.getMaster();

        this.notifications.sendAnswer(master.toString(), new Answer(answering, answer));
        return false;
    }

    @MessageMapping("/answer")
    public void answer(AnswerEvaluation answer, Principal principal) {
        UserPrincipal up = (UserPrincipal) principal;
        if (!up.getID().equals(this.game.getMaster())) {
            return;
        }

        this.game.answerQuestion(this.game.getPlayerByName(answer.getPlayerName()), !answer.isCorrect());

        this.notifications.sendLobbyUpdate(null);
        this.notifications.sendBoardUpdate(null);

    }

    @MessageMapping("/question")
    @SendToUser("/topic/question")
    public String question(QuestionIdentifier identifier, Principal principal) {
        UserPrincipal up = (UserPrincipal) principal;
        Player active = this.game.getCurrentPlayer();

        if (!active.getUuid().equals(up.getID())) {
            return "Du bist nicht an der Reihe!";
        }

        if (!this.game.selectQuestion(identifier)) {
            return "Fehler bei der Auswahl der Frage!";
        }

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
        if (!up.getID().equals(this.game.getMaster())) {
            return;
        }

        game.closeQuestion();

        this.notifications.sendQuestionUpdate(null);
        this.notifications.sendLobbyUpdate(null);
        this.notifications.sendBoardUpdate(null);
    }
}
