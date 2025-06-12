package de.ciupka.jeopardy.game.questions;

public class Question extends AbstractQuestion {

    public Question(String question, int points, String answer) {
        super(question, points, answer, Type.NORMAL);
    }


    @Override
    public boolean allowWrongAnswer() {
        return false;
    }

}
