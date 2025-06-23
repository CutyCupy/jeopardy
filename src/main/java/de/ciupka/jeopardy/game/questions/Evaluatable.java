package de.ciupka.jeopardy.game.questions;

public interface Evaluatable<T> {
    // TODO: Give Feedback about right and wrong players?
    public void evaluateAnswers();
}
