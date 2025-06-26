package de.ciupka.jeopardy.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import de.ciupka.jeopardy.controller.messages.AnswerUpdate;
import de.ciupka.jeopardy.controller.messages.AnswerUpdateType;
import de.ciupka.jeopardy.controller.messages.BoardUpdate;
import de.ciupka.jeopardy.controller.messages.SelectedQuestion;
import de.ciupka.jeopardy.game.GameService;
import de.ciupka.jeopardy.game.questions.AbstractQuestion;
import de.ciupka.jeopardy.game.questions.Answer;

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
    private static final String GAMEMASTER_UPDATE = "/topic/gamemaster-update";
    private static final String BUZZER_UPDATE = "/topic/buzzer-update";
    private static final String ON_BUZZER = "/topic/on-buzzer";
    private static final String ANSWER = "/topic/answer";

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    private GameService game;

    @Autowired
    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    private void message(String topic, Object message, String... users) {
        if (users.length > 0) {
            for (String userId : users) {
                messagingTemplate.convertAndSendToUser(userId, topic, message);
            }
            return;
        }
        messagingTemplate.convertAndSend(topic, message);
    }

    public void sendLobbyUpdate(final String... users) {
        this.message(LOBBY_UPDATE, this.game.getLobby(), users);
    }

    public void sendBoardUpdate(final String... users) {
        this.message(BOARD_UPDATE,
                new BoardUpdate(
                        this.game.getBoard(),
                        this.game.getSelectedQuestion(),
                        this.game.getCurrentPlayer()),
                users);
    }

    public void sendQuestionUpdate(final String... users) {
        SelectedQuestion question = this.game.getSelectedQuestion();
        if (question == null) {
            this.message(QUESTION_UPDATE, new SelectedQuestion(), users);
            return;
        }
        this.message(QUESTION_UPDATE, question, users);
    }

    public void sendGameMasterUpdate(final String... users) {
        this.message(GAMEMASTER_UPDATE, this.game.getMaster() != null, users);
    }

    // TODO: Change to setAnswerControls oder so ...
    public void setBuzzer(boolean value, final String... users) {
        this.message(BUZZER_UPDATE, value, users);
    }

    public void sendOnBuzzer() {
        this.message(ON_BUZZER, new HashMap<>());
    }

    public void sendAnswers(AnswerUpdateType type, final String... users) {
        AbstractQuestion<?> question = game.getSelectedQuestion().getQuestion();

        this.message(ANSWER,
                question.getAnswers().stream()
                        .map((a) -> question.getAnswerUpdate(a, type == null ? a.getUpdateType() : type)).toList(),
                users);
    }
}