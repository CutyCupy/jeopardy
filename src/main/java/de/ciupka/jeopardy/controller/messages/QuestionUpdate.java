package de.ciupka.jeopardy.controller.messages;

import de.ciupka.jeopardy.exception.CategoryNotFoundException;
import de.ciupka.jeopardy.exception.QuestionNotFoundException;
import de.ciupka.jeopardy.game.GameService;
import de.ciupka.jeopardy.game.questions.AbstractQuestion;

public class QuestionUpdate {

    private QuestionIdentifier id;

    private String color;
    private AbstractQuestion<?> question;

    public QuestionUpdate(GameService game) {
        try {
            this.question = game.getSelectedQuestion();
            this.color = this.question.getCategory().getColor();
        } catch (CategoryNotFoundException e) {
        } catch (QuestionNotFoundException e) {
        }
    }

    public QuestionIdentifier getId() {
        return id;
    }

    public String getColor() {
        return color;
    }

    public AbstractQuestion<?> getQuestion() {
        return question;
    }
}
