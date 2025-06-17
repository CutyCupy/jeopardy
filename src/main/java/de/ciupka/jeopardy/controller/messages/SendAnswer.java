package de.ciupka.jeopardy.controller.messages;

import com.fasterxml.jackson.databind.JsonNode;

public class SendAnswer {
    
    private JsonNode answer;

    public SendAnswer(JsonNode answer) {
        this.answer = answer;
    }

    public JsonNode getAnswer() {
        return answer;
    }
}
