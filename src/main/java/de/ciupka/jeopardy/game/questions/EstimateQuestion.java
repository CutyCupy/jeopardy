package de.ciupka.jeopardy.game.questions;

public class EstimateQuestion extends AbstractQuestion {
    public EstimateQuestion(String question, int points, Integer answer) {
        super(question, points, answer.toString(), Type.ESTIMATE);
    }
}
