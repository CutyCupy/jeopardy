package de.ciupka.jeopardy.game.questions;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import de.ciupka.jeopardy.game.Player;

public class EstimateQuestion extends AbstractQuestion<Integer> implements Evaluatable<Integer> {
    public EstimateQuestion(String question, int points, Integer answer) {
        super(question, points, answer, Type.ESTIMATE);
    }

    @Override
    protected Answer<Integer> parseAnswer(JsonNode node, Player player) {
        return new Answer<Integer>(player, node.asInt());
    }

    @Override
    public void evaluateAnswers() {
        int min = Integer.MAX_VALUE;
        List<Answer<Integer>> right = new ArrayList<>();

        for (Answer<Integer> answer : getAnswers()) {
            int delta = Math.abs(answer.getAnswer() - getAnswer());
            if (delta > min) {
                answer.setCorrect(this, false);
                continue;
            }
            if (delta < min) {
                min = delta;
                for (Answer<Integer> ans : right) {
                    ans.setCorrect(this, false);
                }

                right.clear();
            }

            right.add(answer);
        }

        for (Answer<Integer> ans : right) {
            ans.setCorrect(this, true);
        }
    }

}
