package de.ciupka.jeopardy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import de.ciupka.jeopardy.configs.UserPrincipal;
import de.ciupka.jeopardy.controller.messages.AnswerEvaluation;
import de.ciupka.jeopardy.controller.messages.QuestionIdentifier;
import de.ciupka.jeopardy.controller.messages.SubmittedAnswer;
import de.ciupka.jeopardy.exception.AnswerNotFoundException;
import de.ciupka.jeopardy.exception.CategoryNotFoundException;
import de.ciupka.jeopardy.exception.EvaluatableQuestionAnsweredException;
import de.ciupka.jeopardy.exception.GameAlreadyStartedException;
import de.ciupka.jeopardy.exception.InvalidQuestionStateException;
import de.ciupka.jeopardy.exception.NoQuestionSelectedException;
import de.ciupka.jeopardy.exception.NotGameMasterException;
import de.ciupka.jeopardy.exception.NotPlayersTurnException;
import de.ciupka.jeopardy.exception.PlayerAlreadyExistsException;
import de.ciupka.jeopardy.exception.PlayerNotFoundException;
import de.ciupka.jeopardy.exception.QuestionAlreadyAnsweredException;
import de.ciupka.jeopardy.exception.QuestionAlreadySelectedException;
import de.ciupka.jeopardy.exception.QuestionNotFoundException;
import de.ciupka.jeopardy.exception.RevealException;
import de.ciupka.jeopardy.game.GameService;
import de.ciupka.jeopardy.game.Player;
import de.ciupka.jeopardy.game.questions.AbstractQuestion;
import de.ciupka.jeopardy.game.questions.Evaluatable;
import de.ciupka.jeopardy.game.questions.Type;
import de.ciupka.jeopardy.game.questions.answer.Answer;
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
     * @throws PlayerAlreadyExistsException
     */
    @MessageMapping("/join")
    @SendToUser("/topic/join")
    public boolean join(String name, UserPrincipal principal) throws PlayerAlreadyExistsException {
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
    public void startGame(UserPrincipal principal) throws NotGameMasterException, GameAlreadyStartedException {
        if (!principal.getID().equals(this.game.getMaster())) {
            throw new NotGameMasterException();
        }

        this.game.start();

        this.notifications.sendBoardUpdate();
        this.notifications.sendActivePlayerUpdate();
    }

    @MessageMapping("/submit-answer")
    public boolean submitAnswer(SubmittedAnswer answer, UserPrincipal principal)
            throws PlayerNotFoundException, NoQuestionSelectedException, CategoryNotFoundException,
            QuestionNotFoundException {
        Player answering = this.game.getPlayerByID(principal.getID());
        if (answering == null) {
            throw new PlayerNotFoundException(principal.getID());
        }

        AbstractQuestion<?> question = this.game.getSelectedQuestion();
        if (question == null) {
            throw new NoQuestionSelectedException();
        }

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
    public void answer(AnswerEvaluation answerEval, UserPrincipal principal)
            throws NotGameMasterException, NoQuestionSelectedException, EvaluatableQuestionAnsweredException,
            AnswerNotFoundException, CategoryNotFoundException, QuestionNotFoundException {
        if (!principal.getID().equals(this.game.getMaster())) {
            throw new NotGameMasterException();
        }

        AbstractQuestion<?> question = game.getSelectedQuestion();
        if (question == null) {
            throw new NoQuestionSelectedException();
        }

        if (question instanceof Evaluatable) {
            throw new EvaluatableQuestionAnsweredException(question.getType());
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
    public void revealAnswer(String player, UserPrincipal principal)
            throws InvalidQuestionStateException, NotGameMasterException, NoQuestionSelectedException,
            CategoryNotFoundException, QuestionNotFoundException, AnswerNotFoundException {
        if (!principal.getID().equals(this.game.getMaster())) {
            throw new NotGameMasterException();
        }

        AbstractQuestion<?> question = game.getSelectedQuestion();
        if (question == null) {
            throw new NoQuestionSelectedException();
        }

        if (!question.isLocked()) {
            throw new InvalidQuestionStateException();
        }

        Answer<?> answer = question.getAnswerByPlayer(game.getPlayerByName(player));

        answer.setRevealed(true);

        this.notifications.sendAnswers();
    }

    @MessageMapping("/remove-answer")
    public void removeAnswer(String name, UserPrincipal principal) throws NotGameMasterException,
            CategoryNotFoundException, QuestionNotFoundException, NoQuestionSelectedException, PlayerNotFoundException {
        if (!principal.getID().equals(this.game.getMaster())) {
            throw new NotGameMasterException();
        }

        AbstractQuestion<?> question = game.getSelectedQuestion();
        if (question == null) {
            throw new NoQuestionSelectedException();
        }

        Player player = game.getPlayerByName(name);
        if (player == null) {
            throw new PlayerNotFoundException(name);
        }

        question.removeAnswer(player);

        this.notifications.sendQuestionUpdate(player.getUuid());
        this.notifications.sendAnswers();
    }

    @MessageMapping("/question")
    @SendToUser("/topic/question")
    public String question(QuestionIdentifier identifier, UserPrincipal principal)
            throws NotPlayersTurnException, QuestionAlreadyAnsweredException, QuestionAlreadySelectedException,
            CategoryNotFoundException, QuestionNotFoundException {
        Player active = this.game.getCurrentPlayer();

        if (!active.getUuid().equals(principal.getID())) {
            throw new NotPlayersTurnException();
        }
        this.game.selectQuestion(identifier);

        this.notifications.sendQuestionUpdate();
        this.notifications.sendBoardUpdate();

        return null;
    }

    @MessageMapping("/gamemaster")
    @SendToUser("/topic/gamemaster")
    public boolean setGameMaster(UserPrincipal principal) {
        this.game.setMaster(principal.getID());

        this.notifications.sendGameMasterUpdate();

        if (game.isActive()) {
            notifications.sendBoardUpdate(principal.getID());
            notifications.sendQuestionUpdate(principal.getID());
            notifications.sendActivePlayerUpdate(principal.getID());
        }

        return true;
    }

    @MessageMapping("/reset-question")
    public void resetQuestion(UserPrincipal principal)
            throws NotGameMasterException, NoQuestionSelectedException, CategoryNotFoundException,
            QuestionNotFoundException, QuestionAlreadyAnsweredException, RevealException {
        if (!principal.getID().equals(this.game.getMaster())) {
            throw new NotGameMasterException();
        }

        game.resetQuestion();

        this.notifications.sendBoardUpdate();
        this.notifications.sendQuestionUpdate();
        this.notifications.sendActivePlayerUpdate();
        this.notifications.sendAnswers();

    }

    @MessageMapping("/lock-question")
    public void lockQuestion(UserPrincipal principal)
            throws NotGameMasterException, NoQuestionSelectedException, CategoryNotFoundException,
            QuestionNotFoundException, QuestionAlreadyAnsweredException, RevealException {
        if (!principal.getID().equals(this.game.getMaster())) {
            throw new NotGameMasterException();
        }

        AbstractQuestion<?> question = this.game.getSelectedQuestion();
        if (question == null) {
            throw new NoQuestionSelectedException();
        }

        question.setLocked(true);

        this.notifications.sendQuestionUpdate();
    }

    @MessageMapping("/reveal-question")
    public void revealQuestion(boolean more, UserPrincipal principal)
            throws NotGameMasterException, NoQuestionSelectedException, CategoryNotFoundException,
            QuestionNotFoundException, QuestionAlreadyAnsweredException, RevealException {
        if (!principal.getID().equals(this.game.getMaster())) {
            throw new NotGameMasterException();
        }

        AbstractQuestion<?> question = this.game.getSelectedQuestion();
        if (question == null) {
            throw new NoQuestionSelectedException();
        }

        boolean worked = more ? question.revealMore() : question.revealLess();
        if (!worked) {
            throw new RevealException("Dieser Revealschritt ist nicht möglich!");
        }

        if (more && question.isAnswered() && question instanceof Evaluatable eval) {
            eval.evaluateAnswers();
            this.notifications.sendLobbyUpdate();
        }

        this.notifications.sendQuestionUpdate();
        this.notifications.sendActivePlayerUpdate();
        this.notifications.sendAnswers();
    }

    @MessageMapping("/close-question")
    public void closeQuestion(boolean more, UserPrincipal principal)
            throws NotGameMasterException, NoQuestionSelectedException, CategoryNotFoundException,
            QuestionNotFoundException, QuestionAlreadyAnsweredException, RevealException {
        if (!principal.getID().equals(this.game.getMaster())) {
            throw new NotGameMasterException();
        }

        AbstractQuestion<?> question = this.game.getSelectedQuestion();
        if (question == null) {
            throw new NoQuestionSelectedException();
        }

        game.closeQuestion();
    }

}
