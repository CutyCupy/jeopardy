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

public class VideoQuestion extends AbstractQuestion<String> {

    @JsonView(Views.Common.class)
    private final String questionVideo;

    @JsonView(Views.Common.class)
    private final String answerVideo;

    @JsonCreator
    public VideoQuestion(@JsonProperty("question") String question,
            @JsonProperty("points") int points,
            @JsonProperty("answer") String answer,
            @JsonProperty("answerVideo") String answerVideo,
            @JsonProperty("questionVideo") String questionVideo) {
        super(question, points, answer, Type.VIDEO);
        this.questionVideo = questionVideo;
        this.answerVideo = answerVideo;

        getGroups().get(GroupType.QUESTION)
                .addStep(new Step(StepType.VIDEO, questionVideo));

        getGroups().get(GroupType.ANSWER)
                .addStep(new Step(StepType.VIDEO, answerVideo));
    }

    public String getQuestionVideo() {
        return this.questionVideo;
    }

    @Override
    protected Answer<String> parseAnswer(JsonNode node, Player player) {
        return new Answer<>(player, node.asText());
    }
}
