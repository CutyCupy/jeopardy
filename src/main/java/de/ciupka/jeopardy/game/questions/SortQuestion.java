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
    public boolean allowMultipleAnswer() {
        return true;
    }

    @Override
    public boolean allowWrongAnswer() {
        return false;
    }

    @Override
    public Answer<String[]> parseAnswer(JsonNode node, Player player) {
        ObjectMapper mapper = new ObjectMapper();
        return new Answer<String[]>(player, mapper.convertValue(node, String[].class));
    }

    @Override
    public void evaluateAnswers() {
        for (Answer<String[]> answer : getAnswers()) {
            if (Arrays.equals(answer.getAnswer(), getAnswer())) {
                answer.getPlayer().updateScore(getPoints());
            } else {
                answer.getPlayer().updateScore(getWrongPoints());
            }
        }
        setAnswered(true);
    }

    public String[] getOptions() {
        return options;
    }

}
