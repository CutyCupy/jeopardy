package de.ciupka.jeopardy.game.questions;

public class TextQuestion extends AbstractQuestion {

    public TextQuestion(String question, int points, String answer) {
        super(question, points, answer, Type.TEXT);
    }

    @Override
    public boolean allowWrongAnswer() {
        return true;
    }

    @Override
    public boolean allowMultipleAnswer() {
        return false;
    }

}
