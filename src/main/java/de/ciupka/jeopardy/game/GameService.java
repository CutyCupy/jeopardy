package de.ciupka.jeopardy.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import de.ciupka.jeopardy.controller.messages.QuestionIdentifier;
import de.ciupka.jeopardy.controller.messages.SelectedQuestion;
import de.ciupka.jeopardy.game.questions.AbstractQuestion;
import de.ciupka.jeopardy.game.questions.EstimateQuestion;
import de.ciupka.jeopardy.game.questions.Question;
import de.ciupka.jeopardy.game.questions.TextQuestion;

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
                new Category("Allgemeinwissen",
                        new EstimateQuestion("Wie viele Kontinente gibt es auf der Erde?", 100,
                                7),
                        new Question("Welcher Planet ist der dritte von der Sonne?", 200,
                                "Die Erde"),
                        new TextQuestion(
                                "Welcher deutsche Bundeskanzler leitete die Wiedervereinigung 1990?",
                                300,
                                "Helmut Kohl"),
                        new Question("Wie lautet der chemische Name für Kochsalz?", 600,
                                "Natriumchlorid"),
                        new Question("Welches Jahr markierte das Ende des Römischen Reiches im Westen?",
                                1000,
                                "476 n. Chr.")),

                new Category("Filme & Serien",
                        new Question("Wer spielt Harry Potter in den Filmen?", 100,
                                "Daniel Radcliffe"),
                        new Question("Wie heißt der fiktive Kontinent in Game of Thrones?", 200,
                                "Westeros"),
                        new Question("In welchem Film sagt ein Roboter „Ich komme wieder“?",
                                300, "Terminator"),
                        new Question("Wer führte Regie bei Pulp Fiction?", 600,
                                "Quentin Tarantino"),
                        new Question("Wie heißt der Serienmörder in Das Schweigen der Lämmer?",
                                1000,
                                "Hannibal Lecter")),

                new Category("Wissenschaft & Technik",
                        new Question("Was misst man mit einem Thermometer?", 100, "Temperatur"),
                        new Question("Wie viele Beine hat ein Insekt?", 200, "Sechs"),
                        new Question("Wer formulierte die Relativitätstheorie?", 300,
                                "Albert Einstein"),
                        new Question("Wie lautet das Formelzeichen für elektrische Spannung?",
                                600, "U"),
                        new Question("Was bezeichnet der Begriff 'event horizon' in der Astronomie?",
                                1000,
                                "Den Rand eines Schwarzen Lochs")),

                new Category("Musik",
                        new Question("Wer sang Thriller?", 100, "Michael Jackson"),
                        new Question("Welche Band besteht aus John, Paul, George und Ringo?",
                                200, "Die Beatles"),
                        new Question("Welches Instrument hat 88 Tasten?", 300, "Klavier"),
                        new Question("Welcher Komponist war im 18. Jahrhundert als Wunderkind bekannt?",
                                600,
                                "Wolfgang Amadeus Mozart"),
                        new Question("Welcher Künstler gewann 2024 den Grammy für 'Album des Jahres'?",
                                1000,
                                "Taylor Swift")),

                new Category("Geographie",
                        new Question("In welchem Land liegt Paris?", 100, "Frankreich"),
                        new Question("Wie heißt die Hauptstadt von Italien?", 200, "Rom"),
                        new Question("Welcher Fluss fließt durch Budapest?", 300, "Donau"),
                        new Question("Welches ist das flächenmäßig größte Land der Welt?", 600,
                                "Russland"),
                        new Question("Welcher Staat grenzt an die meisten Länder?", 1000,
                                "China")),

                new Category("Sprache & Literatur",
                        new Question("Wie viele Buchstaben hat das deutsche Alphabet (inkl. Umlaute und ß)?",
                                100,
                                "30"),
                        new Question("Wer schrieb Faust?", 200, "Johann Wolfgang von Goethe"),
                        new Question("Was ist ein Synonym für 'schnell'?", 300, "Rasch"),
                        new Question("In welcher Sprache wurde Don Quijote ursprünglich geschrieben?",
                                600, "Spanisch"),
                        new Question("Welcher Autor schrieb Der Name der Rose?", 1000,
                                "Umberto Eco"))
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
        sorted.sort((a, b) -> a.getScore() - b.getScore());
        return this.players.toArray(new Player[this.players.size()]);
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

    public boolean selectQuestion(QuestionIdentifier id) {
        if (this.selectedQuestion != null) {
            return false; // TODO: Maybe use Exceptions for error handling different cases?
        }

        Category cat = getCategory(id.getCategory());
        if (cat == null) {
            return false; // TODO: Maybe use Exceptions for error handling different cases?
        }
        AbstractQuestion qst = cat.getQuestion(id.getQuestion());
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

    public void answerQuestion(Player p, boolean wrong) {
        if (this.selectedQuestion == null) {
            return;
        }

        if (p != null) {
            AbstractQuestion q = selectedQuestion.getQuestion();
            int factor = 1;
            if (wrong) {
                factor = q.allowWrongAnswer() ? 0 : -1;
            }
            p.updateScore(factor * q.getPoints());
        }
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
}
