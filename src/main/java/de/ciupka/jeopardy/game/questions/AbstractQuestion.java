package de.ciupka.jeopardy.game.questions;

import com.fasterxml.jackson.databind.JsonNode;

import de.ciupka.jeopardy.game.Player;

public abstract class AbstractQuestion<T> {
    private String question;
    private int points;
    private T answer;
    private Type type;
    private boolean answered;

    public AbstractQuestion(String question, int points, T answer, Type type) {
        this.question = question;
        this.points = points;
        this.answer = answer;
        this.type = type;
    }

    public int getPoints() {
        return points;
    }

    public String getQuestion() {
        return question;
    }

    public T getAnswer() {
        return answer;
    }

    public boolean isAnswered() {
        return this.answered;
    }

    public void setAnswered(boolean answered) {
        this.answered = answered;
    }

    public Type getType() {
        return this.type;
    }

    protected void giveOrTakePoints(Player p, boolean wrong) {
        int factor = 1;
        if (wrong) {
            factor = allowWrongAnswer() ? 0 : -1;
        }
        p.updateScore(factor * getPoints());
    }

    public abstract boolean allowWrongAnswer();

    public abstract boolean allowMultipleAnswer();

    public abstract T parseAnswer(JsonNode node);
}
