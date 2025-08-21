package de.ciupka.jeopardy.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import de.ciupka.jeopardy.controller.messages.AnswersUpdate;
import de.ciupka.jeopardy.controller.messages.BoardUpdate;
import de.ciupka.jeopardy.controller.messages.LobbyUpdate;
import de.ciupka.jeopardy.controller.messages.Notification;
import de.ciupka.jeopardy.controller.messages.QuestionIdentifier;
import de.ciupka.jeopardy.controller.messages.QuestionUpdate;
import de.ciupka.jeopardy.exception.CategoryNotFoundException;
import de.ciupka.jeopardy.exception.QuestionNotFoundException;
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
    private static final String ON_BUZZER = "/topic/on-buzzer";
    private static final String ANSWER = "/topic/answer";

    private static final String NOTIFICATION = "/queue/notification";

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    private GameService game;

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
        this.message(LOBBY_UPDATE, new LobbyUpdate(game), users);
    }

    public void sendBoardUpdate(UUID... users) {
        if (users.length == 0) {
            users = game.getPlayerIDs();
        }

        for (UUID user : users) {
            this.message(BOARD_UPDATE,
                    new BoardUpdate(this.game, this.game.getPlayerByID(user)),
                    user);
        }

        this.message(BOARD_UPDATE,
                new BoardUpdate(this.game, null),
                this.game.getMaster());
    }

    public void sendQuestionUpdate(final UUID... users) {
        QuestionIdentifier question = this.game.getSelectedQuestionIdentifier();
        if (question == null) {
            this.message(QUESTION_UPDATE, new HashMap<>(), users);
            return;
        }
        this.message(QUESTION_UPDATE, new QuestionUpdate(game), users);
    }

    public void sendGameMasterUpdate(final UUID... users) {
        this.message(GAMEMASTER_UPDATE, this.game.getMaster() != null, users);
    }

    public void sendOnBuzzer() {
        this.message(ON_BUZZER, new HashMap<>());
    }

    public void sendAnswers() {
        try {
            AbstractQuestion<?> selected = game.getSelectedQuestion();
            this.message(ANSWER,
                    new AnswersUpdate(selected, false),
                    game.getPlayerIDs());

            this.message(ANSWER,
                    new AnswersUpdate(selected, true),
                    game.getMaster());
            return;
        } catch (CategoryNotFoundException e) {
        } catch (QuestionNotFoundException e) {
        }
        this.message(ANSWER, new AnswersUpdate());
        return;

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
        this.message(ACTIVE_PLAYER_UPDATE, true, p.getId());
        this.message(ACTIVE_PLAYER_UPDATE, false,
                Arrays.stream(users).filter((v) -> !p.getId().equals(v)).toArray(UUID[]::new));
    }

    public void sendNotification(Notification noti, UUID... users) {
        this.message(NOTIFICATION, noti, users);
    }

}