package de.ciupka.jeopardy.game.questions;

import com.fasterxml.jackson.databind.JsonNode;

public class VideoQuestion extends AbstractQuestion<String> {
    private String path;

    public VideoQuestion(String question, int points, String answer, String path) {
        super(question, points, answer, Type.VIDEO);
        this.path = path;
    }

    public String getPath() {
        return this.path;
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
    public String parseAnswer(JsonNode node) {
        return node.asText();
    }
}
