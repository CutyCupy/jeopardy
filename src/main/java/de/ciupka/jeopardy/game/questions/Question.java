package de.ciupka.jeopardy.game.questions;

import com.fasterxml.jackson.databind.JsonNode;

import de.ciupka.jeopardy.game.Category;
import de.ciupka.jeopardy.game.Player;
import de.ciupka.jeopardy.game.questions.answer.Answer;
import de.ciupka.jeopardy.game.questions.reveal.GroupType;
import de.ciupka.jeopardy.game.questions.reveal.Step;
import de.ciupka.jeopardy.game.questions.reveal.StepType;

public class Question extends AbstractQuestion<String> {

    public Question(Category category, String question, int points, String answer) {
        super(category, question, points, answer, Type.NORMAL);

        getGroups().get(GroupType.ANSWER)
                .addStep(new Step(StepType.TEXT, "Antwort: " + answer));
    }

    @Override
    protected Answer<String> parseAnswer(JsonNode node, Player player) {
        return new Answer<>(player, node.asText());
    }
}
