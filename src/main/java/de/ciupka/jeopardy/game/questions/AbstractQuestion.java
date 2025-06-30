package de.ciupka.jeopardy.game.questions;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

import de.ciupka.jeopardy.exception.AnswerNotFoundException;
import de.ciupka.jeopardy.exception.RevealException;
import de.ciupka.jeopardy.game.Category;
import de.ciupka.jeopardy.game.Player;
import de.ciupka.jeopardy.game.questions.answer.Answer;
import de.ciupka.jeopardy.game.questions.reveal.Group;
import de.ciupka.jeopardy.game.questions.reveal.GroupType;
import de.ciupka.jeopardy.game.questions.reveal.Step;
import de.ciupka.jeopardy.game.questions.reveal.StepType;

public abstract class AbstractQuestion<T> {

    private final Type type;
    private final int points;
    private final T answer;

    private boolean locked;

    @JsonIgnore
    private final Category category;

    private final Map<GroupType, Group> groups = new EnumMap<>(GroupType.class);

    @JsonIgnore
    private final List<Answer<T>> answers = new ArrayList<>();

    public AbstractQuestion(Category category, String questionText, int points, T answer, Type type) {
        this.category = category;
        this.points = points;
        this.answer = answer;
        this.type = type;

        initDefaultGroups(questionText);
    }

    private void initDefaultGroups(String questionText) {
        groups.put(GroupType.METADATA, new Group(GroupType.METADATA)
                .addStep(new Step(StepType.TEXT, String.format("%s - %d Punkte", category.getName(), points)))
                .addStep(new Step(StepType.TEXT, type.getTitle())));

        groups.put(GroupType.QUESTION, new Group(GroupType.QUESTION)
                .addStep(new Step(StepType.TEXT, questionText)));

        groups.put(GroupType.HINT, new Group(GroupType.HINT));
        groups.put(GroupType.ANSWER, new Group(GroupType.ANSWER));
    }

    public T getAnswer() {
        return answer;
    }

    public int getPoints() {
        return points;
    }

    public int getWrongPoints() {
        return type.getHasPenalty() ? 0 : -getPoints();
    }

    public boolean isAnswered() {
        if (answers.stream().anyMatch(a -> a.getCorrect() == null)) {
            return false;
        }
        return groups.get(GroupType.ANSWER).isComplete();
    }

    public Type getType() {
        return type;
    }

    public Map<GroupType, Group> getGroups() {
        return groups;
    }

    public boolean revealMore() throws RevealException {
        for (GroupType type : GroupType.values()) {
            Group grp = groups.get(type);
            Step step = grp.getNextStep();
            if (step == null) {
                continue;
            }

            switch (type) {
                case ANSWER:
                    if (!grp.isStarted() && !isLocked()) {
                        throw new RevealException("Die Frage ist noch nicht gelocked!");
                    }
            }

            step.setRevealed(true);
            return true;

        }
        return false;
    }

    public boolean revealLess() {
        GroupType[] values = GroupType.values();
        for (int i = values.length - 1; i >= 0; i--) {
            Group grp = groups.get(values[i]);
            Step step = grp.getLatestStep();
            if (step == null) {
                continue;
            }
            step.setRevealed(false);
            return true;
        }
        return false;
    }

    protected abstract Answer<T> parseAnswer(JsonNode node, Player player);

    public void addAnswer(Player player, JsonNode node) {
        Answer<T> newAnswer = parseAnswer(node, player);
        getAnswers()
                .stream()
                .filter(a -> a.getPlayer().equals(player))
                .findFirst()
                .ifPresentOrElse(
                        a -> a.setAnswer(newAnswer.getAnswer()),
                        () -> answers.add(newAnswer));
    }

    public void removeAnswer(Player player) {
        answers.removeIf(a -> a.getPlayer().equals(player));
    }

    public boolean hasAnswered(Player p) {
        return answers.stream().anyMatch(a -> a.getPlayer().equals(p));
    }

    public Answer<T> getAnswerByPlayer(Player p) throws AnswerNotFoundException {
        return answers.stream()
                .filter(a -> a.getPlayer().equals(p))
                .findFirst()
                .orElseThrow(() -> new AnswerNotFoundException(p));
    }

    public List<Answer<T>> getAnswers() {
        return answers;
    }

    public void setLocked(boolean locked) throws RevealException {
        this.locked = locked;
    }

    public boolean isLocked() {
        return locked;
    }

    public Category getCategory() {
        return category;
    }

    public void reset() {
        this.answers.clear();

        for (Group grp : groups.values()) {
            grp.reset();
        }
    }
}
