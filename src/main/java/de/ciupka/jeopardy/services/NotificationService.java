package de.ciupka.jeopardy.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import de.ciupka.jeopardy.controller.messages.BoardUpdate;
import de.ciupka.jeopardy.controller.messages.QuestionIdentifier;
import de.ciupka.jeopardy.controller.messages.SelectedQuestion;
import de.ciupka.jeopardy.game.Category;
import de.ciupka.jeopardy.game.GameService;
import de.ciupka.jeopardy.game.Player;
import de.ciupka.jeopardy.game.questions.AbstractQuestion;
import jakarta.annotation.Nullable;

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
                        this.game.getCurrentQuestionIdentifier(),
                        this.game.getCurrentPlayer()));
    }

    public void sendQuestionUpdate(final String userId) {
        Player active = this.game.getCurrentPlayer();

        QuestionIdentifier identifier = this.game.getCurrentQuestionIdentifier();
        if (identifier == null) {
            this.message(userId, QUESTION_UPDATE, new SelectedQuestion());
            return;
        }

        Category cat = this.game.getCategory(identifier.getCategory());
        AbstractQuestion qst = cat.getQuestion(identifier.getQuestion());

        this.message(userId, QUESTION_UPDATE, new SelectedQuestion(cat, qst, active));
    }

    public void sendGameMasterUpdate(final String userId) {
        this.message(userId, GAMEMASTER_UPDATE, this.game.getMaster() != null);
    }
}