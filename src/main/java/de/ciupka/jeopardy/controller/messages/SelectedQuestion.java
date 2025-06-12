package de.ciupka.jeopardy.controller.messages;

import de.ciupka.jeopardy.game.Category;
import de.ciupka.jeopardy.game.Player;
import de.ciupka.jeopardy.game.questions.AbstractQuestion;

public class SelectedQuestion {

    private Category category;
    private AbstractQuestion question;
    private Player selectedBy;

    public SelectedQuestion() {
    }

    public SelectedQuestion(Category category, AbstractQuestion question, Player selectedBy) {
        this.category = category;
        this.question = question;
        this.selectedBy = selectedBy;
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
}
