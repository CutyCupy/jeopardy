package de.ciupka.jeopardy.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

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
import de.ciupka.jeopardy.game.questions.EstimateQuestion;
import de.ciupka.jeopardy.game.questions.Question;
import de.ciupka.jeopardy.game.questions.SortQuestion;
import de.ciupka.jeopardy.game.questions.TextQuestion;
import de.ciupka.jeopardy.game.questions.VideoQuestion;
import de.ciupka.jeopardy.game.questions.answer.Answer;
import de.ciupka.jeopardy.game.questions.answer.SortOption;
import de.ciupka.jeopardy.game.questions.answer.SortOptions;

@Service
public class GameService {
    private List<Player> players;

    private Category[] board;
    private QuestionIdentifier selectedQuestionIdentifier;

    private int currentPlayerIdx = -1;
    private UUID master;

    public GameService() throws CategoryNotFoundException {
        this.players = new ArrayList<>();

        Category twitch = new Category("Twitch", "#4D3280");
        twitch.addQuestion(
                new EstimateQuestion(
                        twitch,
                        "Wieviele Nachrichten habe ich in Chats im Mai 2025 von Streamern aus unserer Freundesgruppe geschrieben? (Sleep, Chris, Lasse, Leonie, Lari, Selina)",
                        100, 88 + 16 + 613 + 1703 + 14 + 308));
        twitch.addQuestion(new Question(twitch, "Wie teuer ist ein Tier-3 Sub aktuell?", 400,
                "19,99€ (zumindest wenn ich bei Attix jetzt Tier-3 subben würde)"));
        twitch.addQuestion(new SortQuestion(twitch,
                "Ordne die folgenden Streamer basierend auf ihren Subs (von den Meisten zu den Wenigsten)",
                700,
                new SortOptions(
                        new SortOption("Papaplatte", 5000),
                        new SortOption("NoWay4U_Sir", 4000),
                        new SortOption("Gronkh", 3000),
                        new SortOption("Tolkin", 2000),
                        new SortOption("RvNxMango", 1000),
                        new SortOption("Mahluna", 0000))));
        twitch.addQuestion(new TextQuestion(
                twitch,
                "Welches Emote wurde 2021 aufgrund von kontroversen Tweets des 'Originals' entfernt?",
                1000,
                "PogChamp"));
        Category tierwelt = new Category("Tierwelt", "#506837");
        tierwelt.addQuestion(
                new SortQuestion(tierwelt, "Sortiere diese Tiere nach ihrer Größe (die Größten zuerst)", 100,
                        new SortOptions(
                                new SortOption("Elefant", 100),
                                new SortOption("Pferd", 80),
                                new SortOption("Schaf", 60),
                                new SortOption("Katze", 40),
                                new SortOption("Igel", 20),
                                new SortOption("Ameise", 10))));
        tierwelt.addQuestion(
                new EstimateQuestion(tierwelt, "Wieviel Kilogramm Krill ist ein Blauwal pro Tag im Schnitt?", 400,
                        7000));
        tierwelt.addQuestion(new EstimateQuestion(tierwelt,
                "Wie schnell war die schnellste aufgezeichnete Hauskatze (in km/h)?", 700,
                48));
        tierwelt.addQuestion(new Question(tierwelt, "Wieviele Mägen hat eine Kuh?", 1000, "Vier"));

        Category lol = new Category("League of Legends",
                "#9E8C49");

        lol.addQuestion(new Question(lol, "Wieviele Schwänze hat Ahri?", 100, "9"));
        lol.addQuestion(new EstimateQuestion(lol, "Wieviel Range hat Caitlyn?", 400, 650));
        lol.addQuestion(new TextQuestion(lol, "Welchen Champion spielte Faker in seinem Pro Debüt?", 700, "Nidalee"));
        lol.addQuestion(new VideoQuestion(lol, "Was passiert als nächstes?", 1000, "2dz8zb",
                "Blaber flashed und stirbt für die Krabbe", "ga4ln4"));

        Category valo = new Category("Valorant",
                "#B93B3B");
        valo.addQuestion(new Question(valo, "Wieviele Waffen gibt es in VALORANT?", 100, "18"));
        valo.addQuestion(new SortQuestion(valo,
                "Ordne die folgenden Waffen basierend auf ihre Magazingröße (von den Meisten zu den Wenigsten)",
                400,
                new SortOptions(
                        new SortOption("Odin", 100),
                        new SortOption("Phantom", 30),
                        new SortOption("Vandal", 25),
                        new SortOption("Guardian", 12),
                        new SortOption("Sheriff", 6),
                        new SortOption("Operator", 5))));
        valo.addQuestion(new Question(valo, "Welcher Agent kommt aus Schweden?", 700, "Breach"));
        valo.addQuestion(new EstimateQuestion(valo,
                "Wieviel HP hat die Harbor Sphere (oder Smoke - ka wie man das Ding nennen soll)",
                1000, 500));

        this.board = new Category[] {
                twitch, tierwelt, lol, valo
        };
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
            throw new QuestionAlreadyAnsweredException();
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
}
