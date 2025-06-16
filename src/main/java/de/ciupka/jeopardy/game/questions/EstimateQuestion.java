package de.ciupka.jeopardy.game.questions;

public class EstimateQuestion extends AbstractQuestion {
    public EstimateQuestion(String question, int points, Integer answer) {
        super(question, points, answer.toString(), Type.ESTIMATE);
    }

    @Override
    public boolean allowMultipleAnswer() {
        return true;
    }

    @Override
    public boolean allowWrongAnswer() {
        return true;
    }
}
