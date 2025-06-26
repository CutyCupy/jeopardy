package de.ciupka.jeopardy.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import de.ciupka.jeopardy.controller.messages.BoardUpdate;
import de.ciupka.jeopardy.controller.messages.SelectedQuestion;
import de.ciupka.jeopardy.game.GameService;
import de.ciupka.jeopardy.game.Player;
import de.ciupka.jeopardy.game.questions.AbstractQuestion;

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
    private static final String ACTIVE_PLAYER_UPDATE = "/topic/active-player-update";
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

    private void message(String topic, Object message, UUID... users) {
        if (users.length > 0) {
            for (UUID userId : users) {
                messagingTemplate.convertAndSendToUser(userId.toString(), topic, message);
            }
            return;
        }
        messagingTemplate.convertAndSend(topic, message);
    }

    public void sendLobbyUpdate(final UUID... users) {
        this.message(LOBBY_UPDATE, this.game.getLobby(), users);
    }

    public void sendBoardUpdate(UUID... users) {
        if (users.length == 0) {
            users = game.getPlayerIDs();
        }

        for (UUID user : users) {
            this.message(BOARD_UPDATE,
                    new BoardUpdate(
                            this.game.getBoard(),
                            this.game.getSelectedQuestion(),
                            this.game.getCurrentPlayer(),
                            this.game.getPlayerByID(user)),
                    user);
        }

        this.message(BOARD_UPDATE,
                new BoardUpdate(
                        this.game.getBoard(),
                        this.game.getSelectedQuestion(),
                        this.game.getCurrentPlayer(),
                        null),
                this.game.getMaster());
    }

    public void sendQuestionUpdate(final UUID... users) {
        SelectedQuestion question = this.game.getSelectedQuestion();
        if (question == null) {
            this.message(QUESTION_UPDATE, new SelectedQuestion(), users);
            return;
        }
        this.message(QUESTION_UPDATE, question, users);
    }

    public void sendGameMasterUpdate(final UUID... users) {
        this.message(GAMEMASTER_UPDATE, this.game.getMaster() != null, users);
    }

    // TODO: Change to setAnswerControls oder so ...
    public void setBuzzer(boolean value, final UUID... users) {
        this.message(BUZZER_UPDATE, value, users);
    }

    public void sendOnBuzzer() {
        this.message(ON_BUZZER, new HashMap<>());
    }

    public void sendAnswers(final UUID... users) {
        AbstractQuestion<?> question = game.getSelectedQuestion().getQuestion();

        this.message(ANSWER,
                question.getAnswerUpdates(),
                users);
    }

    public void sendActivePlayerUpdate(UUID... users) {
        Player p = game.getCurrentPlayer();
        if (users.length == 0) {
            users = game.getPlayerIDs();
        }
        if (p == null) {
            this.message(ACTIVE_PLAYER_UPDATE, false, users);
            return;
        }
        this.message(ACTIVE_PLAYER_UPDATE, true, p.getUuid());
        this.message(ACTIVE_PLAYER_UPDATE, true,
                Arrays.stream(users).filter((v) -> !p.getUuid().equals(v)).toArray(UUID[]::new));
    }
}