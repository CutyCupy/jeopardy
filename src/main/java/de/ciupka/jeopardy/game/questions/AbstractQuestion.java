package de.ciupka.jeopardy.game.questions;

public abstract class AbstractQuestion {
    private String question;
    private int points;
    private String answer;
    private Type type;
    private boolean answered;

    public AbstractQuestion(String question, int points, String answer, Type type) {
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

    public String getAnswer() {
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
}
