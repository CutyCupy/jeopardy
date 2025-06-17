package de.ciupka.jeopardy.game.questions;

import java.util.List;

import de.ciupka.jeopardy.controller.messages.Answer;

public interface Evaluatable<T> {
    // TODO: Give Feedback about right and wrong players?
    public void evaluateAnswers(List<Answer<T>> answers);
}
