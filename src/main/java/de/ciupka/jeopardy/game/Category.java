package de.ciupka.jeopardy.game;

import java.util.ArrayList;
import java.util.List;

import de.ciupka.jeopardy.exception.CategoryNotFoundException;
import de.ciupka.jeopardy.exception.QuestionNotFoundException;
import de.ciupka.jeopardy.game.questions.AbstractQuestion;

public class Category {

    private String name;
    private String colorCode;
    private List<AbstractQuestion<?>> questions;

    public Category(String name, String colorCode) {
        this.name = name;
        this.colorCode = colorCode;
        this.questions = new ArrayList<>();
    }

    public void addQuestion(AbstractQuestion<?> question) throws CategoryNotFoundException {
        if (!question.getCategory().equals(this)) {
            // TODO: Wrong Category
            throw new CategoryNotFoundException();
        }
        questions.add(question);
    }

    public String getName() {
        return name;
    }

    public List<AbstractQuestion<?>> getQuestions() {
        return questions;
    }

    public AbstractQuestion<?> getQuestion(int idx) throws QuestionNotFoundException {
        if (idx < 0 || idx >= this.questions.size()) {
            throw new QuestionNotFoundException();
        }
        return this.questions.get(idx);
    }

    public String getColorCode() {
        return colorCode;
    }
}
