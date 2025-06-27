package de.ciupka.jeopardy.game.questions;

import com.fasterxml.jackson.databind.JsonNode;

import de.ciupka.jeopardy.game.Player;

public class Question extends AbstractQuestion<String> {

    public Question(String question, int points, String answer) {
        super(question, points, answer, Type.NORMAL);
    }

    @Override
    protected Answer<String> parseAnswer(JsonNode node, Player player) {
        return new Answer<String>(player, node.asText());
    }
}
