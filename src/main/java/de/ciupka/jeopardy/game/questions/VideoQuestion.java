package de.ciupka.jeopardy.game.questions;

import com.fasterxml.jackson.databind.JsonNode;
import de.ciupka.jeopardy.game.Player;
import de.ciupka.jeopardy.game.Category;
import de.ciupka.jeopardy.game.questions.answer.Answer;
import de.ciupka.jeopardy.game.questions.reveal.GroupType;
import de.ciupka.jeopardy.game.questions.reveal.Step;
import de.ciupka.jeopardy.game.questions.reveal.StepType;

public class VideoQuestion extends AbstractQuestion<String> {
    private final String path;

    public VideoQuestion(Category category, String question, int points, String answer, String answerVideo, String path) {
        super(category, question, points, answer, Type.VIDEO);
        this.path = path;

        getGroups().get(GroupType.QUESTION)
                .addStep(new Step(StepType.VIDEO, path));

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
