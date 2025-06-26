package de.ciupka.jeopardy.game.questions;

import com.fasterxml.jackson.databind.JsonNode;

import de.ciupka.jeopardy.game.Player;

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
    public boolean allowWrongAnswer() {
        return true;
    }

    @Override
    protected Answer<String> parseAnswer(JsonNode node, Player player) {
        return new Answer<String>(player, node.asText());
    }
}
