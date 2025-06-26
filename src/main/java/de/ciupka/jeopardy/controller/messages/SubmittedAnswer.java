package de.ciupka.jeopardy.controller.messages;

import com.fasterxml.jackson.databind.JsonNode;

public class SubmittedAnswer {

    private JsonNode answer;
    private String player;

    public SubmittedAnswer(JsonNode answer, String player) {
        this.answer = answer;
        this.player = player;
    }

    public JsonNode getAnswer() {
        return answer;
    }

    public String getPlayer() {
        return player;
    }
}
