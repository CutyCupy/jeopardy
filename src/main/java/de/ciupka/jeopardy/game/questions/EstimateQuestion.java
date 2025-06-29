package de.ciupka.jeopardy.game.questions;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import de.ciupka.jeopardy.game.Category;
import de.ciupka.jeopardy.game.Player;
import de.ciupka.jeopardy.game.questions.answer.Answer;
import de.ciupka.jeopardy.game.questions.reveal.GroupType;
import de.ciupka.jeopardy.game.questions.reveal.Step;
import de.ciupka.jeopardy.game.questions.reveal.StepType;

public class EstimateQuestion extends AbstractQuestion<Integer> implements Evaluatable<Integer> {

    public EstimateQuestion(Category category, String question, int points, Integer answer) {
        super(category, question, points, answer, Type.ESTIMATE);

        getGroups().get(GroupType.ANSWER)
                .addStep(new Step(StepType.TEXT, "Korrekte Schätzung: " + answer));
    }

    @Override
    protected Answer<Integer> parseAnswer(JsonNode node, Player player) {
        return new Answer<>(player, node.asInt());
    }

    @Override
    public void evaluateAnswers() {
        int minDelta = Integer.MAX_VALUE;
        List<Answer<Integer>> closest = new ArrayList<>();

        for (Answer<Integer> answer : getAnswers()) {
            int delta = Math.abs(answer.getAnswer() - getAnswer());

            if (delta > minDelta) {
                answer.setCorrect(this, false);
                continue;
            }

            if (delta < minDelta) {
                for (Answer<Integer> a : closest) {
                    a.setCorrect(this, false);
                }
                closest.clear();
                minDelta = delta;
            }

            closest.add(answer);
        }

        for (Answer<Integer> correctAnswer : closest) {
            correctAnswer.setCorrect(this, true);
        }
    }
}
