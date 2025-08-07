package de.ciupka.jeopardy.game.questions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;

import de.ciupka.jeopardy.configs.Views;
import de.ciupka.jeopardy.game.Player;
import de.ciupka.jeopardy.game.questions.answer.Answer;
import de.ciupka.jeopardy.game.questions.reveal.GroupType;
import de.ciupka.jeopardy.game.questions.reveal.Step;
import de.ciupka.jeopardy.game.questions.reveal.StepType;

public class HintQuestion extends AbstractQuestion<String> {

    @JsonView(Views.Common.class)
    private String[] hints;

    @JsonCreator
    public HintQuestion(
        @JsonProperty("question") String question, 
        @JsonProperty("points") int points, 
        @JsonProperty("answer") String answer, 
        @JsonProperty("hints") String[] hints) {
        super(question, points, answer, Type.HINT);

        this.hints = hints;

        for (String hint : hints) {
            this.getGroups().get(GroupType.HINT).addStep(new Step(StepType.TEXT, hint));
        }
    }

    @Override
    protected Answer<String> parseAnswer(JsonNode node, Player player) {
        return new Answer<>(player, node.asText());
    }

    public String[] getHints() {
        return hints;
    }
}
