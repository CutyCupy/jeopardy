package de.ciupka.jeopardy.game.questions;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import de.ciupka.jeopardy.controller.messages.Answer;
import de.ciupka.jeopardy.game.Player;

public class EstimateQuestion extends AbstractQuestion<Integer> implements Evaluatable<Integer> {
    public EstimateQuestion(String question, int points, Integer answer) {
        super(question, points, answer, Type.ESTIMATE);
    }

    @Override
    public boolean allowMultipleAnswer() {
        return true;
    }

    @Override
    public boolean allowWrongAnswer() {
        return true;
    }

    @Override
    public Integer parseAnswer(JsonNode node) {
        return node.asInt();
    }

    @Override
    public void evaluateAnswers(List<Answer<Integer>> answers) {
        int min = Integer.MAX_VALUE;
        List<Player> right = new ArrayList<>();

        for (Answer<Integer> answer : answers) {
            int delta = Math.abs(answer.getAnswer() - getAnswer());
            if (delta > min) {
                giveOrTakePoints(answer.getPlayer(), false);
                continue;
            }
            if (delta < min) {
                min = delta;
                for (Player p : right) {
                    giveOrTakePoints(p, false);
                }

                right.clear();
            }

            right.add(answer.getPlayer());
        }

        for (Player p : right) {
            giveOrTakePoints(p, true);
        }
    }
}
