package de.ciupka.jeopardy.game.questions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ciupka.jeopardy.controller.messages.Answer;
import de.ciupka.jeopardy.game.Player;

public class SortQuestion extends AbstractQuestion<String[]> implements Evaluatable<String[]> {

    private String[] options;

    public SortQuestion(String question, int points, String[] answer) {
        super(question, points, answer, Type.SORT);

        List<String> options = new ArrayList<>(List.of(answer));
        Collections.shuffle(options);

        this.options = options.toArray(new String[options.size()]);
    }

    @Override
    public boolean allowWrongAnswer() {
        return true;
    }

    @Override
    public Answer<String[]> parseAnswer(JsonNode node, Player player) {
        ObjectMapper mapper = new ObjectMapper();
        return new Answer<String[]>(player, mapper.convertValue(node, String[].class));
    }

    @Override
    public void evaluateAnswers() {
        int max = -1;
        List<Player> right = new ArrayList<>();

        String[] correctAnswer = getAnswer();

        for (Answer<String[]> answer : getAnswers()) {
            int corrects = 0;
            String[] ans = answer.getAnswer();
            for (int i = 0; i < ans.length; i++) {
                if (ans[i].equals(correctAnswer[i])) {
                    corrects++;
                }
            }
            if (corrects < max) {
                answer.getPlayer().updateScore(getWrongPoints());
                continue;
            }
            if (corrects > max) {
                max = corrects;
                for (Player p : right) {
                    p.updateScore(getWrongPoints());
                }

                right.clear();
            }

            right.add(answer.getPlayer());
        }

        for (Player p : right) {
            p.updateScore(getPoints()); // TODO: Maybe use a factor for the Points?
        }

    }

    public String[] getOptions() {
        return options;
    }

}
