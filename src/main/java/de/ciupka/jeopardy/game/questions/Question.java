package de.ciupka.jeopardy.game.questions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import de.ciupka.jeopardy.game.Player;
import de.ciupka.jeopardy.game.questions.answer.Answer;
import de.ciupka.jeopardy.game.questions.answer.Tool;
import de.ciupka.jeopardy.game.questions.reveal.GroupType;
import de.ciupka.jeopardy.game.questions.reveal.Step;
import de.ciupka.jeopardy.game.questions.reveal.StepType;

public class Question extends AbstractQuestion<String> {

    @JsonCreator
    public Question(@JsonProperty("question") String question,
            @JsonProperty("points") int points,
            @JsonProperty("answer") String answer,
            @JsonProperty("answerTool") Tool answerTool) {
        super(question, points, answer, Type.NORMAL, answerTool);

        getGroups().get(GroupType.ANSWER)
                .addStep(new Step(StepType.TEXT, "Antwort: " + answer));
    }

    @Override
    protected Answer<String> parseAnswer(JsonNode node, Player player) {
        return new Answer<>(player, node.asText());
    }
}
