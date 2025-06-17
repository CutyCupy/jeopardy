package de.ciupka.jeopardy.game;

import de.ciupka.jeopardy.game.questions.AbstractQuestion;

public class Category {

    private String name;
    private AbstractQuestion<?>[] questions;

    public Category(String name, AbstractQuestion<?>... questions) {
        this.name = name;
        this.questions = questions;
    }

    public String getName() {
        return name;
    }

    public AbstractQuestion<?>[] getQuestions() {
        return questions;
    }

    public AbstractQuestion<?> getQuestion(int idx) {
        if (idx < 0 || idx >= this.questions.length) {
            return null; // Maybe throw an Exception instead?
        }
        return this.questions[idx];
    }
}
