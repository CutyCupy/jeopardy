package de.ciupka.jeopardy.game.questions;

import com.fasterxml.jackson.databind.JsonNode;

import de.ciupka.jeopardy.game.Player;

public class TextQuestion extends AbstractQuestion<String> {

    public TextQuestion(String question, int points, String answer) {
        super(question, points, answer, Type.TEXT);
    }

    @Override
    protected Answer<String> parseAnswer(JsonNode node, Player player) {
        return new Answer<String>(player, node.asText());
    }
}
