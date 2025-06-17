package de.ciupka.jeopardy.game.questions;

import com.fasterxml.jackson.databind.JsonNode;

public class Question extends AbstractQuestion<String> {

    public Question(String question, int points, String answer) {
        super(question, points, answer, Type.NORMAL);
    }


    @Override
    public boolean allowWrongAnswer() {
        return false;
    }

    @Override
    public boolean allowMultipleAnswer() {
        return false;
    }

    @Override
    public String parseAnswer(JsonNode node) {
        return node.asText();
    }

}
