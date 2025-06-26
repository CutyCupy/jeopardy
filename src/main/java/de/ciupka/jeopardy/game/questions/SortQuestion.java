package de.ciupka.jeopardy.game.questions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ciupka.jeopardy.controller.messages.Answer;
import de.ciupka.jeopardy.game.Player;

public class SortQuestion extends AbstractQuestion<SortOption[]>
        implements Evaluatable<SortOption[]> {

    private String[] options;

    public SortQuestion(String question, int points, SortOption[] answer) {
        super(question, points, answer, Type.SORT);

        List<String> options = new ArrayList<>(List.of(answer).stream().map((t) -> t.getName()).toList());
        Collections.shuffle(options);

        this.options = options.toArray(new String[options.size()]);
    }

    @Override
    public boolean allowWrongAnswer() {
        return true;
    }

    @Override
    public Answer<SortOption[]> parseAnswer(JsonNode node, Player player) {
        ObjectMapper mapper = new ObjectMapper();
        return new Answer<SortOption[]>(player, mapper.convertValue(node, SortOption[].class));
    }

    @Override
    public void evaluateAnswers() {
        int max = -1;
        List<Player> right = new ArrayList<>();

        SortOption[] correctAnswer = getAnswer();

        for (Answer<SortOption[]> answer : getAnswers()) {
            int corrects = 0;
            SortOption[] ans = answer.getAnswer();
            for (int i = 0; i < ans.length; i++) {
                if (ans[i].getName().equals(correctAnswer[i].getName())) {
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
