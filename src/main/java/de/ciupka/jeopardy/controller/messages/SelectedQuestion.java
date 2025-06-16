package de.ciupka.jeopardy.controller.messages;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.ciupka.jeopardy.game.Category;
import de.ciupka.jeopardy.game.Player;
import de.ciupka.jeopardy.game.questions.AbstractQuestion;

public class SelectedQuestion {

    private QuestionIdentifier identifier;
    private Category category;
    private AbstractQuestion question;
    private Player selectedBy;

    @JsonIgnore
    private List<Answer> answers;

    public SelectedQuestion() {
    }

    public SelectedQuestion(QuestionIdentifier id, Category cat, AbstractQuestion qst,
            Player selectedBy) {
        this.identifier = id;
        this.category = cat;
        this.question = qst;
        this.selectedBy = selectedBy;
    }

    public QuestionIdentifier getIdentifier() {
        return identifier;
    }

    public Category getCategory() {
        return category;
    }

    public AbstractQuestion getQuestion() {
        return question;
    }

    public Player getSelectedBy() {
        return selectedBy;
    }

    public boolean addAnswer(Answer answer) {
        Optional<Answer> existing = this.answers.stream().filter((v) -> v.getPlayer().equals(answer.getPlayer()))
                .findFirst();
        if (existing.isPresent()) {
            return false; // TODO: Maybe Update answer if allowed by question?
        }
        this.answers.add(answer);
        return true;
    }
}
