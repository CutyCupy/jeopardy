package de.ciupka.jeopardy.game.questions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

import de.ciupka.jeopardy.controller.messages.AnswerUpdate;
import de.ciupka.jeopardy.game.Player;

public abstract class AbstractQuestion<T> {
    private String question;
    private int points;
    private T answer;
    private Type type;
    private boolean answered;

    private QuestionState state;

    @JsonIgnore
    private List<Answer<T>> answers = new ArrayList<>();

    public AbstractQuestion(String question, int points, T answer, Type type) {
        this.question = question;
        this.points = points;
        this.answer = answer;
        this.type = type;
        this.state = QuestionState.HIDDEN;
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

    protected abstract Answer<T> parseAnswer(JsonNode node, Player player);

    public List<AnswerUpdate> getAnswerUpdates() {
        return answers.stream().map((a) -> {
            switch (a.getUpdateType()) {
                case SHORT_ANSWER, FULL_ANSWER:
                    return new AnswerUpdate(a.getPlayer().getName(), a.getAnswer().toString());
                default:
                    return new AnswerUpdate(a.getPlayer().getName(), null);
            }
        }).toList();
    }

    public QuestionState getState() {
        return state;
    }

    public boolean revealMore() {
        QuestionState old = state;
        state = state.next();
        return !old.equals(state);
    }

    public boolean revealLess() {
        QuestionState old = state;
        state = state.previous();
        return !old.equals(state);
    }

    public List<Answer<T>> getAnswers() {
        return answers;
    }

    public Answer<T> getAnswerByPlayer(Player p) {
        Optional<Answer<T>> existing = answers.stream().filter((a) -> a.getPlayer().equals(p))
                .findFirst();
        return existing.isPresent() ? existing.get() : null;
    }

    public void addAnswer(Player p, JsonNode answer) {
        Optional<Answer<T>> existing = answers.stream().filter((a) -> a.getPlayer().equals(p))
                .findFirst();

        Answer<T> newAnswer = parseAnswer(answer, p);

        if (existing.isPresent()) {
            existing.get().setAnswer(newAnswer.getAnswer());
            return;
        }
        answers.add(newAnswer);
    }
}
