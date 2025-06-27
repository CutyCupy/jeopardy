package de.ciupka.jeopardy.controller.messages;

import de.ciupka.jeopardy.game.Category;
import de.ciupka.jeopardy.game.GameService;
import de.ciupka.jeopardy.game.questions.AbstractQuestion;

public class QuestionUpdate {

    private QuestionIdentifier id;

    private Category category;
    private AbstractQuestion<?> question;

    public QuestionUpdate(QuestionIdentifier id, GameService game) {
        this.id = id;
        this.category = game.getCategory(id.getCategory());
        if (this.category != null) {
            this.question = this.category.getQuestion(id.getQuestion());
        }
    }

    public QuestionIdentifier getId() {
        return id;
    }

    public Category getCategory() {
        return category;
    }

    public AbstractQuestion<?> getQuestion() {
        return question;
    }
}
