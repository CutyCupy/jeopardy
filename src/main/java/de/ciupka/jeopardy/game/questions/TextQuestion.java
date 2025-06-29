package de.ciupka.jeopardy.game.questions;

import com.fasterxml.jackson.databind.JsonNode;
import de.ciupka.jeopardy.game.Player;
import de.ciupka.jeopardy.game.Category;
import de.ciupka.jeopardy.game.questions.answer.Answer;
import de.ciupka.jeopardy.game.questions.reveal.GroupType;
import de.ciupka.jeopardy.game.questions.reveal.Step;
import de.ciupka.jeopardy.game.questions.reveal.StepType;

public class TextQuestion extends AbstractQuestion<String> {

    public TextQuestion(Category category, String question, int points, String answer) {
        super(category, question, points, answer, Type.TEXT);

        getGroups().get(GroupType.ANSWER)
            .addStep(new Step(StepType.TEXT, answer));
    }

    @Override
    protected Answer<String> parseAnswer(JsonNode node, Player player) {
        return new Answer<>(player, node.asText());
    }
}
