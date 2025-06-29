package de.ciupka.jeopardy.game.questions.reveal;

import java.util.ArrayList;
import java.util.List;

public class Group {

    private GroupType type;
    private List<Step> steps;

    public Group(GroupType type) {
        this.type = type;
        this.steps = new ArrayList<>();
    }

    public Group addStep(Step step) {
        this.steps.add(step);
        return this;
    }

    public GroupType getType() {
        return type;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public boolean isComplete() {
        return this.steps.stream().allMatch(Step::isRevealed);
    }

    public boolean isStarted() {
        return this.steps.stream().anyMatch(Step::isRevealed);
    }

    public Step getNextStep() {
        return steps.stream().filter(s -> !s.isRevealed()).findFirst().orElse(null);
    }

    public Step getLatestStep() {
        return steps.stream()
                .filter(s -> s.isRevealed())
                .reduce((first, second) -> second).orElse(null);
    }

    public void reset() {
        for (Step step : steps) {
            step.setRevealed(false);
        }
    }
}
