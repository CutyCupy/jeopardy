package de.ciupka.jeopardy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import de.ciupka.jeopardy.configs.UserPrincipal;
import de.ciupka.jeopardy.controller.messages.AnswerEvaluation;
import de.ciupka.jeopardy.controller.messages.AnswerUpdateType;
import de.ciupka.jeopardy.controller.messages.QuestionIdentifier;
import de.ciupka.jeopardy.controller.messages.SubmittedAnswer;
import de.ciupka.jeopardy.game.GameService;
import de.ciupka.jeopardy.game.Player;
import de.ciupka.jeopardy.game.questions.AbstractQuestion;
import de.ciupka.jeopardy.game.questions.Answer;
import de.ciupka.jeopardy.game.questions.Evaluatable;
import de.ciupka.jeopardy.game.questions.QuestionState;
import de.ciupka.jeopardy.game.questions.Type;
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
    public void onConnect(UserPrincipal principal) {
        this.notifications.sendGameMasterUpdate(principal.getID());
        this.notifications.sendLobbyUpdate(principal.getID());
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
    public boolean join(String name, UserPrincipal principal) {
        final boolean added = this.game.addPlayer(principal.getID(), name);

        /**
         * TODO: Might be necessary to send more data on lobby updates.
         * In general it might be smart to also send the 'reason' for a lobby update
         * with extra data based on reasoning.
         */
        notifications.sendLobbyUpdate();
        if (game.isActive()) {
            notifications.sendBoardUpdate(principal.getID());
            notifications.sendQuestionUpdate(principal.getID());
            notifications.sendActivePlayerUpdate(principal.getID());
        }

        return added;
    }

    @MessageMapping("/start-game")
    public void startGame(UserPrincipal principal) {
        if (!principal.getID().equals(this.game.getMaster()) || this.game.isActive()) {
            return;
        }

        this.game.start();

        this.notifications.sendBoardUpdate();
        this.notifications.sendActivePlayerUpdate();
    }

    @MessageMapping("/submit-answer")
    public boolean submitAnswer(SubmittedAnswer answer, UserPrincipal principal) {
        Player answering = this.game.getPlayerByID(principal.getID());
        if (answering == null) {
            return false;
        }

        AbstractQuestion<?> question = this.game.getSelectedQuestion().getQuestion();

        question.addAnswer(answering, answer.getAnswer());

        // TODO: This can be checked better
        if (question.getType().equals(Type.NORMAL)) {
            this.notifications.sendOnBuzzer();
            this.notifications.setBuzzer(false, principal.getID());
        }

        this.notifications.sendAnswers();

        return false;
    }

    @MessageMapping("/answer")
    public void answer(AnswerEvaluation answerEval, UserPrincipal principal) {
        if (!principal.getID().equals(this.game.getMaster())) {
            return;
        }

        AbstractQuestion<?> question = game.getSelectedQuestion().getQuestion();
        if (question instanceof Evaluatable) {
            return;
        }

        Player player = this.game.getPlayerByName(answerEval.getPlayerName());

        if (this.game.answerQuestion(player, !answerEval.isCorrect())) {
            this.notifications.sendLobbyUpdate();
            this.notifications.sendBoardUpdate();
            this.notifications.sendAnswers();
        }
    }

    @MessageMapping("/reveal-answer")
    @SendToUser("/topic/reveal-answer")
    public String revealAnswer(String player, UserPrincipal principal) {
        if (!principal.getID().equals(this.game.getMaster())) {
            return "Nur der Gamemaster darf Antworten revealen!";
        }

        AbstractQuestion<?> question = game.getSelectedQuestion().getQuestion();

        if (question.getState().ordinal() < QuestionState.LOCK_QUESTION.ordinal()) {
            return "Die Frage ist noch nicht locked!";
        }

        Answer<?> answer = question.getAnswerByPlayer(game.getPlayerByName(player));

        answer.setUpdateType(AnswerUpdateType.SHORT_ANSWER);

        this.notifications.sendAnswers();

        return null;
    }

    @MessageMapping("/question")
    @SendToUser("/topic/question")
    public String question(QuestionIdentifier identifier, UserPrincipal principal) {
        Player active = this.game.getCurrentPlayer();

        if (!active.getUuid().equals(principal.getID())) {
            return "Du bist nicht an der Reihe!";
        }

        if (!this.game.selectQuestion(identifier)) {
            return "Fehler bei der Auswahl der Frage!";
        }

        this.notifications.sendQuestionUpdate();
        this.notifications.sendBoardUpdate();

        return null;
    }

    @MessageMapping("/gamemaster")
    @SendToUser("/topic/gamemaster")
    public boolean setGameMaster(UserPrincipal principal) {
        this.game.setMaster(principal.getID());

        this.notifications.sendGameMasterUpdate();

        return true;
    }

    @MessageMapping("/reveal-question")
    public void lockQuestion(boolean more, UserPrincipal principal) {
        if (!principal.getID().equals(this.game.getMaster())) {
            return;
        }

        AbstractQuestion<?> question = game.getSelectedQuestion().getQuestion();

        boolean worked = more ? question.revealMore() : question.revealLess();

        if (worked) {
            if (more && question.getState().equals(QuestionState.SHOW_ANSWER) && question instanceof Evaluatable) {
                ((Evaluatable<?>) question).evaluateAnswers();
                this.notifications.sendLobbyUpdate();
            }
        } else {
            if (question.getState().equals(QuestionState.HIDDEN)) {
                game.resetQuestion();
            } else {
                game.closeQuestion();
            }
            this.notifications.sendLobbyUpdate();
            this.notifications.sendBoardUpdate();
        }

        this.notifications.sendQuestionUpdate();
        this.notifications.sendActivePlayerUpdate();
        this.notifications.sendAnswers();
    }

}
