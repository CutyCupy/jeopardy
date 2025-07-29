package de.ciupka.jeopardy.game.questions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import de.ciupka.jeopardy.game.Player;
import de.ciupka.jeopardy.game.questions.answer.Answer;
import de.ciupka.jeopardy.game.questions.reveal.GroupType;
import de.ciupka.jeopardy.game.questions.reveal.Step;
import de.ciupka.jeopardy.game.questions.reveal.StepType;

public class VideoQuestion extends AbstractQuestion<String> {
    private final String path;

    @JsonCreator
    public VideoQuestion(@JsonProperty("question") String question,
    @JsonProperty("points") int points,
    @JsonProperty("answer") String answer,
    @JsonProperty("answerVideo") String answerVideo,
    @JsonProperty("questionVideo") String questionVideo) {
        super(question, points, answer, Type.VIDEO);
        this.path = questionVideo;

        getGroups().get(GroupType.QUESTION)
                .addStep(new Step(StepType.VIDEO, questionVideo));

        getGroups().get(GroupType.ANSWER)
                .addStep(new Step(StepType.VIDEO, answerVideo));
    }

    public String getPath() {
        return this.path;
    }

    @Override
    protected Answer<String> parseAnswer(JsonNode node, Player player) {
        return new Answer<>(player, node.asText());
    }
}
