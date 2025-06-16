package de.ciupka.jeopardy.services;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import de.ciupka.jeopardy.controller.messages.Answer;
import de.ciupka.jeopardy.controller.messages.BoardUpdate;
import de.ciupka.jeopardy.controller.messages.SelectedQuestion;
import de.ciupka.jeopardy.game.GameService;

/**
 * This service provides utility methods for communication with all or specific
 * WebSocket clients.
 * 
 * @author Alexander Ciupka
 */
@Service
public class NotificationService {

    private static final String LOBBY_UPDATE = "/topic/lobby-update";
    private static final String BOARD_UPDATE = "/topic/board-update";
    private static final String QUESTION_UPDATE = "/topic/question-update";
    private static final String ANSWER_UPDATE = "/topic/answer-update";
    private static final String GAMEMASTER_UPDATE = "/topic/gamemaster-update";
    private static final String BUZZER_UPDATE = "/topic/buzzer-update";
    private static final String ANSWER = "/topic/answer";
    private static final String LOCK_QUESTION = "/topic/lock-question";

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    private GameService game;

    @Autowired
    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    private void message(final String userId, String topic, Object message) {
        if (userId != null) {
            messagingTemplate.convertAndSendToUser(userId, topic, message);
            return;
        }
        messagingTemplate.convertAndSend(topic, message);
    }

    public void sendLobbyUpdate(final String userId) {
        this.message(userId, LOBBY_UPDATE, this.game.getLobby());
    }

    public void sendBoardUpdate(final String userId) {
        this.message(userId, BOARD_UPDATE,
                new BoardUpdate(
                        this.game.getBoard(),
                        this.game.getSelectedQuestion(),
                        this.game.getCurrentPlayer()));
    }

    public void sendQuestionUpdate(final String userId) {
        SelectedQuestion question = this.game.getSelectedQuestion();
        if (question == null) {
            this.message(userId, QUESTION_UPDATE, new SelectedQuestion());
            return;
        }
        this.message(userId, QUESTION_UPDATE, question);
    }

    public void sendAnswerUpdate(final String userId) {
        SelectedQuestion question = this.game.getSelectedQuestion();
        if (question == null) {
            this.message(userId, QUESTION_UPDATE, new SelectedQuestion());
            return;
        }
        this.message(userId, ANSWER_UPDATE, question);
    }

    public void sendGameMasterUpdate(final String userId) {
        this.message(userId, GAMEMASTER_UPDATE, this.game.getMaster() != null);
    }

    public void setBuzzer(String userId, boolean value) {
        this.message(userId, BUZZER_UPDATE, value);
    }

    public void sendAnswer(String userId, Answer answer) {
        this.message(userId, ANSWER, answer);
    }

    public void lockQuestion() {
        this.message(null, LOCK_QUESTION, new HashMap<>());
    }
}