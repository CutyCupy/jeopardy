package de.ciupka.jeopardy.game.questions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ciupka.jeopardy.controller.messages.Answer;

public class SortQuestion extends AbstractQuestion<String[]> implements Evaluatable<String[]> {

    private List<String> options;

    public SortQuestion(String question, int points, String[] answer) {
        super(question, points, answer, Type.SORT);

        options = new ArrayList<>(List.of(answer));
        Collections.shuffle(options);
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
    public String[] parseAnswer(JsonNode node) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(node, String[].class);
    }

    @Override
    public void evaluateAnswers(List<Answer<String[]>> answers) {
        for (Answer<String[]> answer : answers) {
            giveOrTakePoints(answer.getPlayer(), !Arrays.equals(answer.getAnswer(), getAnswer()));
        }
        setAnswered(true);
    }

}
