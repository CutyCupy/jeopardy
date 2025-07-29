package de.ciupka.jeopardy.game;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ciupka.jeopardy.controller.messages.QuestionIdentifier;
import de.ciupka.jeopardy.exception.AnswerNotFoundException;
import de.ciupka.jeopardy.exception.CategoryNotFoundException;
import de.ciupka.jeopardy.exception.GameAlreadyStartedException;
import de.ciupka.jeopardy.exception.NoQuestionSelectedException;
import de.ciupka.jeopardy.exception.PlayerAlreadyExistsException;
import de.ciupka.jeopardy.exception.QuestionAlreadyAnsweredException;
import de.ciupka.jeopardy.exception.QuestionAlreadySelectedException;
import de.ciupka.jeopardy.exception.QuestionNotFoundException;
import de.ciupka.jeopardy.game.questions.AbstractQuestion;
import de.ciupka.jeopardy.game.questions.Evaluatable;
import de.ciupka.jeopardy.game.questions.answer.Answer;

@Service
public class GameService {
    private List<Player> players;

    private Category[] board;
    private QuestionIdentifier selectedQuestionIdentifier;

    private int currentPlayerIdx = -1;
    private UUID master;

    public GameService() throws CategoryNotFoundException, IOException {
        this.players = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream inputStream = GameService.class.getResourceAsStream("/questions.json")) {
            if (inputStream == null) {
                throw new IllegalStateException("questions.json not found in resources!");
            }

            this.board = mapper.readValue(inputStream, new TypeReference<Category[]>() {
            });
        }
    }

    /**
     * addPlayer either adds a {@code Player} to the lobby or updates the
     * {@code uuid} of the existing {@code Player} when someone with {@code name}
     * already exists.
     * 
     * @param uuid The {@code UUID} of the Websocket Client that tries to be added
     *             to the lobby.
     * @param name The name that the new player wants to have.
     * @return True if a player was added to the lobby. If a player already exists
     *         with {@code name}, it will return false.
     * @throws PlayerAlreadyExistsException
     */
    public void addPlayer(UUID uuid, String name) throws PlayerAlreadyExistsException {
        Optional<Player> player = this.players.stream().filter((p) -> p.getName().equals(name))
                .findFirst();
        if (player.isPresent()) {
            Player existing = player.get();
            if (!existing.isDisconnected()) {
                throw new PlayerAlreadyExistsException(existing);
            }
            existing.setId(uuid);
            return;
        }
        this.players.add(new Player(uuid, name));
    }

    public Player[] getLobby() {
        List<Player> sorted = new ArrayList<>(this.players);
        sorted.sort((a, b) -> b.getScore() - a.getScore());
        return sorted.toArray(new Player[sorted.size()]);
    }

    public Category[] getBoard() {
        return this.board;
    }

    public Category getCategory(int idx) throws CategoryNotFoundException {
        if (idx < 0 || idx >= this.board.length) {
            throw new CategoryNotFoundException();
        }
        return this.board[idx];
    }

    public void resetQuestion() {
        try {
            AbstractQuestion<?> question = getSelectedQuestion();
            question.reset();
        } catch (Exception e) {

        }

        this.selectedQuestionIdentifier = null;
    }

    public void selectQuestion(QuestionIdentifier id) throws QuestionAlreadyAnsweredException,
            QuestionAlreadySelectedException, CategoryNotFoundException, QuestionNotFoundException {
        if (this.selectedQuestionIdentifier != null) {
            throw new QuestionAlreadySelectedException();
        }

        Category cat = getCategory(id.getCategory());

        AbstractQuestion<?> qst = cat.getQuestion(id.getQuestion());
        if (qst == null) {
            throw new QuestionNotFoundException();
        }

        if (qst.isAnswered()) {
            throw new QuestionAlreadyAnsweredException();
        }

        this.selectedQuestionIdentifier = id;
    }

    public QuestionIdentifier getSelectedQuestionIdentifier() {
        return selectedQuestionIdentifier;
    }

    public AbstractQuestion<?> getSelectedQuestion() throws CategoryNotFoundException, QuestionNotFoundException {
        if (this.selectedQuestionIdentifier == null) {
            return null;
        }
        Category cat = getCategory(this.selectedQuestionIdentifier.getCategory());

        return cat != null ? cat.getQuestion(this.selectedQuestionIdentifier.getQuestion()) : null;
    }

    public boolean answerQuestion(Player p, boolean wrong) throws NoQuestionSelectedException, AnswerNotFoundException,
            CategoryNotFoundException, QuestionNotFoundException {
        AbstractQuestion<?> q = this.getSelectedQuestion();
        if (q == null) {
            throw new NoQuestionSelectedException();
        }

        Answer<?> answer = q.getAnswerByPlayer(p);

        answer.setCorrect(q, !wrong);
        return true;
    }

    public void closeQuestion()
            throws NoQuestionSelectedException, CategoryNotFoundException, QuestionNotFoundException,
            QuestionAlreadyAnsweredException {
        AbstractQuestion<?> question = this.getSelectedQuestion();
        if (question == null) {
            throw new NoQuestionSelectedException();
        }

        if (!question.isAnswered()) {
            if (question instanceof Evaluatable eval) {
                eval.evaluateAnswers();
            } else {
                throw new QuestionAlreadyAnsweredException();
            }
        }

        selectedQuestionIdentifier = null;
        currentPlayerIdx = (currentPlayerIdx + 1) % this.players.size();
    }

    public Player getCurrentPlayer() {
        if (currentPlayerIdx < 0) {
            return null;
        }
        return this.players.get(currentPlayerIdx);
    }

    public Player getPlayerByID(UUID uid) {
        Optional<Player> result = this.players.stream().filter((p) -> p.getId().equals(uid)).findAny();
        if (result.isPresent()) {
            return result.get();
        }
        return null;
    }

    public Player getPlayerByName(String name) {
        Optional<Player> result = this.players.stream().filter((p) -> p.getName().equals(name)).findAny();
        if (result.isPresent()) {
            return result.get();
        }
        return null;
    }

    public UUID getMaster() {
        return this.master;
    }

    public void setMaster(UUID id) {
        this.master = id;
    }

    public boolean isActive() {
        return this.currentPlayerIdx >= 0;
    }

    public void start() throws GameAlreadyStartedException {
        if (this.isActive()) {
            throw new GameAlreadyStartedException();
        }

        this.currentPlayerIdx = (int) (this.players.size() * Math.random());
    }

    public boolean onDisconnect(UUID id) {
        Optional<Player> player = players.stream().filter((p) -> p.getId().equals(id)).findAny();
        if (player.isPresent()) {
            player.get().disconnect();
            return true;
        }

        if (id.equals(master)) {
            setMaster(null);
            return true;
        }

        return false;
    }

    public UUID[] getPlayerIDs() {
        return players.stream().map((p) -> p.getId()).toArray(UUID[]::new);
    }

    public String getTitle() {
        return "Jeopardy";
    }
}
