package de.ciupka.jeopardy.game.questions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

import de.ciupka.jeopardy.controller.messages.Answer;
import de.ciupka.jeopardy.game.Player;

public abstract class AbstractQuestion<T> {
    private String question;
    private int points;
    private T answer;
    private Type type;
    private boolean answered;

    private RevealState state;

    @JsonIgnore
    private List<Answer<T>> answers = new ArrayList<>();

    public AbstractQuestion(String question, int points, T answer, Type type) {
        this.question = question;
        this.points = points;
        this.answer = answer;
        this.type = type;
        this.state = RevealState.HIDDEN;
    }

    public int getPoints() {
        return points;
    }

     public int getWrongPoints() {
        return allowWrongAnswer() ? 0 : -getPoints();
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


    public abstract boolean allowWrongAnswer();

    public abstract boolean allowMultipleAnswer();

    public abstract T parseAnswer(JsonNode node);

    public RevealState getState() {
        return state;
    }

    public boolean revealMore() {
        RevealState old = state;
        state = state.next();
        return !old.equals(state);
    }

    public boolean revealLess() {
        RevealState old = state;
        state = state.previous();
        return !old.equals(state);
    }

    public List<Answer<T>> getAnswers() {
        return answers;
    }

    public Answer<T> addAnswer(Player p, JsonNode answer) {
        Optional<Answer<T>> existing = answers.stream().filter((a) -> a.getPlayer().equals(p))
                .findFirst();

        T value = parseAnswer(answer);

        if (existing.isPresent()) {
            existing.get().setAnswer(value);
            return existing.get();
        }
        Answer<T> newAnswer = new Answer<T>(p, value);
        answers.add(newAnswer);
        return newAnswer;
    }
}
