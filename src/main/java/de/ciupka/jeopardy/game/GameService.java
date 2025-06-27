package de.ciupka.jeopardy.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import de.ciupka.jeopardy.controller.messages.QuestionIdentifier;
import de.ciupka.jeopardy.controller.messages.SelectedQuestion;
import de.ciupka.jeopardy.game.questions.AbstractQuestion;
import de.ciupka.jeopardy.game.questions.Answer;
import de.ciupka.jeopardy.game.questions.EstimateQuestion;
import de.ciupka.jeopardy.game.questions.Question;
import de.ciupka.jeopardy.game.questions.SortOption;
import de.ciupka.jeopardy.game.questions.SortQuestion;
import de.ciupka.jeopardy.game.questions.TextQuestion;
import de.ciupka.jeopardy.game.questions.VideoQuestion;

@Service
public class GameService {
    private List<Player> players;

    private Category[] board;
    private SelectedQuestion selectedQuestion;

    private int currentPlayerIdx = -1;
    private UUID master;

    public GameService() {
        this.players = new ArrayList<>();

        this.board = new Category[] {
                new Category("Twitch",
                        "#4D3280",
                        new EstimateQuestion(
                                "Wieviele Nachrichten habe ich in Chats im Mai 2025 von Streamern aus unserer Freundesgruppe geschrieben? (Sleep, Chris, Lasse, Leonie, Lari, Selina)",
                                100,
                                88 + 16 + 613 + 1703 + 14 + 308),
                        new Question("Wie teuer ist ein Tier-3 Sub aktuell?", 400,
                                "19,99€ (zumindest wenn ich bei Attix jetzt Tier-3 subben würde)"),
                        new SortQuestion(
                                "Ordne die folgenden Streamer basierend auf ihren Subs (von den Meisten zu den Wenigsten)",
                                700,
                                new SortOption[] {
                                        new SortOption("Papaplatte", 5000),
                                        new SortOption("NoWay4U_Sir", 4000),
                                        new SortOption("Gronkh", 3000),
                                        new SortOption("Tolkin", 2000),
                                        new SortOption("RvNxMango", 1000),
                                        new SortOption("Mahluna", 0000)
                                }),
                        new TextQuestion(
                                "Welches Emote wurde 2021 aufgrund von kontroversen Tweets des 'Originals' entfernt?",
                                1000,
                                "PogChamp")),

                new Category("Tierwelt",
                        "#506837",
                        new SortQuestion("Sortiere diese Tiere nach ihrer Größe (die Größten zuerst)", 100,
                                new SortOption[] {
                                        new SortOption("Elefant", 100),
                                        new SortOption("Pferd", 80),
                                        new SortOption("Schaf", 60),
                                        new SortOption("Katze", 40),
                                        new SortOption("Igel", 20),
                                        new SortOption("Ameise", 10),
                                }),
                        new EstimateQuestion("Wieviel Kilogramm Krill ist ein Blauwal pro Tag im Schnitt?", 400,
                                7000),
                        new EstimateQuestion("Wie schnell war die schnellste aufgezeichnete Hauskatze (in km/h)?", 700,
                                48),
                        new Question("Wieviele Mägen hat eine Kuh?", 1000, "Vier")),

                new Category("League of Legends",
                        "#9E8C49",
                        new Question("Wieviele Schwänze hat Ahri?", 100, "9"),
                        new EstimateQuestion("Wieviel Range hat Caitlyn?", 400, 650),
                        new TextQuestion("Welchen Champion spielte Faker in seinem Pro Debüt?", 700, "Nidalee"),
                        new VideoQuestion("Was passiert als nächstes?", 1000, "2dz8zb", "ga4ln4")),
                new Category("Valorant",
                        "#B93B3B",
                        new Question("Wieviele Waffen gibt es in VALORANT?", 100, "18"),
                        new SortQuestion(
                                "Ordne die folgenden Waffen basierend auf ihre Magazingröße (von den Meisten zu den Wenigsten)",
                                400,
                                new SortOption[] {
                                        new SortOption("Odin", 100),
                                        new SortOption("Phantom", 30),
                                        new SortOption("Vandal", 25),
                                        new SortOption("Guardian", 12),
                                        new SortOption("Sheriff", 6),
                                        new SortOption("Operator", 5)
                                }),
                        new Question("Welcher Agent kommt aus Schweden?", 700, "Breach"),
                        new EstimateQuestion(
                                "Wieviel HP hat die Harbor Sphere (oder Smoke - ka wie man das Ding nennen soll)",
                                1000, 500)),
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
     */
    public boolean addPlayer(UUID uuid, String name) {
        Optional<Player> player = this.players.stream().filter((p) -> p.getName().equals(name))
                .findFirst();
        if (player.isPresent()) {
            // TODO: Think about a better solution for returning players.
            // This Method relies heavily on trust, that noone hijacks another player by
            // simply using their name.
            player.get().setUuid(uuid);
            return false;
        }
        this.players.add(new Player(uuid, name));

        return true;
    }

    public Player[] getLobby() {
        List<Player> sorted = new ArrayList<>(this.players);
        sorted.sort((a, b) -> b.getScore() - a.getScore());
        return sorted.toArray(new Player[sorted.size()]);
    }

    public Category[] getBoard() {
        return this.board;
    }

    public Category getCategory(int idx) {
        if (idx < 0 || idx >= this.board.length) {
            return null; // Maybe throw an Exception instead?
        }
        return this.board[idx];
    }

    public void resetQuestion() {
        this.selectedQuestion = null;
    }

    public boolean selectQuestion(QuestionIdentifier id) {
        if (this.selectedQuestion != null) {
            return false; // TODO: Maybe use Exceptions for error handling different cases?
        }

        Category cat = getCategory(id.getCategory());
        if (cat == null) {
            return false; // TODO: Maybe use Exceptions for error handling different cases?
        }
        AbstractQuestion<?> qst = cat.getQuestion(id.getQuestion());
        if (qst == null) {
            return false; // TODO: Maybe use Exceptions for error handling different cases?
        }

        if (qst.isAnswered()) {
            return false;
        }

        this.selectedQuestion = new SelectedQuestion(id, cat, qst, getCurrentPlayer());
        return true;
    }

    public SelectedQuestion getSelectedQuestion() {
        return selectedQuestion;
    }

    public boolean answerQuestion(Player p, boolean wrong) {
        if (this.selectedQuestion == null) {
            return false;
        }

        AbstractQuestion<?> q = selectedQuestion.getQuestion();

        Answer<?> answer = q.getAnswerByPlayer(p);
        if (answer == null) {
            return false;
        }

        answer.setCorrect(q, !wrong);
        return true;
    }

    public void closeQuestion() {
        if (this.selectedQuestion == null) {
            return;
        }

        selectedQuestion.getQuestion().setAnswered(true);
        selectedQuestion = null;
        currentPlayerIdx = (currentPlayerIdx + 1) % this.players.size();
    }

    public Player getCurrentPlayer() {
        if (currentPlayerIdx < 0) {
            return null;
        }
        return this.players.get(currentPlayerIdx);
    }

    public Player getPlayerByID(UUID uid) {
        Optional<Player> result = this.players.stream().filter((p) -> p.getUuid().equals(uid)).findAny();
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

    public void start() {
        if (this.isActive()) {
            return;
        }

        this.currentPlayerIdx = (int) (this.players.size() * Math.random());
    }

    public boolean onDisconnect(UUID id) {
        Optional<Player> player = players.stream().filter((p) -> p.getUuid().equals(id)).findAny();
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
        return players.stream().map((p) -> p.getUuid()).toArray(UUID[]::new);
    }
}
