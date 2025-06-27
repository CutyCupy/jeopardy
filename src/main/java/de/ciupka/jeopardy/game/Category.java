package de.ciupka.jeopardy.game;

import de.ciupka.jeopardy.exception.QuestionNotFoundException;
import de.ciupka.jeopardy.game.questions.AbstractQuestion;

public class Category {

    private String name;
    private String colorCode;
    private AbstractQuestion<?>[] questions;

    public Category(String name, String colorCode, AbstractQuestion<?>... questions) {
        this.name = name;
        this.colorCode = colorCode;
        this.questions = questions;
    }

    public String getName() {
        return name;
    }

    public AbstractQuestion<?>[] getQuestions() {
        return questions;
    }

    public AbstractQuestion<?> getQuestion(int idx) throws QuestionNotFoundException {
        if (idx < 0 || idx >= this.questions.length) {
            throw new QuestionNotFoundException();
        }
        return this.questions[idx];
    }

    public String getColorCode() {
        return colorCode;
    }
}
