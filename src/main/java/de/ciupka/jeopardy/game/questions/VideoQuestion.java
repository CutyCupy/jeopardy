package de.ciupka.jeopardy.game.questions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;

import de.ciupka.jeopardy.configs.Views;
import de.ciupka.jeopardy.game.Player;
import de.ciupka.jeopardy.game.questions.answer.Answer;
import de.ciupka.jeopardy.game.questions.answer.Tool;
import de.ciupka.jeopardy.game.questions.reveal.GroupType;
import de.ciupka.jeopardy.game.questions.reveal.Step;
import de.ciupka.jeopardy.game.questions.reveal.StepType;

public class VideoQuestion extends AbstractQuestion<String> {

    @JsonView(Views.Common.class)
    private final String questionVideo;

    @JsonView(Views.Common.class)
    private final String answerVideo;

    @JsonView(Views.Common.class)
    private final double blurDuration;

    @JsonCreator
    public VideoQuestion(@JsonProperty("question") String question,
            @JsonProperty("points") int points,
            @JsonProperty("answer") String answer,
            @JsonProperty("answerVideo") String answerVideo,
            @JsonProperty("questionVideo") String questionVideo,
            @JsonProperty("blurDuration") double blurDuration,
            @JsonProperty("answerTool") Tool answerTool) {
        super(question, points, answer, Type.VIDEO, answerTool);
        this.questionVideo = questionVideo;
        this.answerVideo = answerVideo;
        this.blurDuration = blurDuration;

        getGroups().get(GroupType.QUESTION)
                .addStep(new Step(StepType.VIDEO, new Step.VideoData(questionVideo, blurDuration)));

        getGroups().get(GroupType.ANSWER)
                .addStep(new Step(StepType.VIDEO, new Step.VideoData(answerVideo, 0)));
    }

    public String getQuestionVideo() {
        return this.questionVideo;
    }

    public String getAnswerVideo() {
        return answerVideo;
    }

    public double getBlurDuration() {
        return blurDuration;
    }

    @Override
    protected Answer<String> parseAnswer(JsonNode node, Player player) {
        return new Answer<>(player, node.asText());
    }
}
