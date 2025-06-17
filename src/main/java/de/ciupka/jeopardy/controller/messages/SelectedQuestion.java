package de.ciupka.jeopardy.controller.messages;

import de.ciupka.jeopardy.game.Category;
import de.ciupka.jeopardy.game.Player;
import de.ciupka.jeopardy.game.questions.AbstractQuestion;

public class SelectedQuestion {

    private QuestionIdentifier identifier;
    private Category category;
    private AbstractQuestion<?> question;
    private Player selectedBy;

    public SelectedQuestion() {
    }

    public SelectedQuestion(QuestionIdentifier id, Category cat, AbstractQuestion<?> qst,
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

    public AbstractQuestion<?> getQuestion() {
        return question;
    }

    public Player getSelectedBy() {
        return selectedBy;
    }
}
