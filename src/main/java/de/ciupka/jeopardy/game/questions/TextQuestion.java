package de.ciupka.jeopardy.game.questions;

import com.fasterxml.jackson.databind.JsonNode;

public class TextQuestion extends AbstractQuestion<String> {

    public TextQuestion(String question, int points, String answer) {
        super(question, points, answer, Type.TEXT);
    }

    @Override
    public boolean allowWrongAnswer() {
        return true;
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
