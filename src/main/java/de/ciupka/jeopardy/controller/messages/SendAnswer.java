package de.ciupka.jeopardy.controller.messages;

import com.fasterxml.jackson.databind.JsonNode;

public class SendAnswer {

    private JsonNode answer;
    private String player;

    public SendAnswer(JsonNode answer, String player) {
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
