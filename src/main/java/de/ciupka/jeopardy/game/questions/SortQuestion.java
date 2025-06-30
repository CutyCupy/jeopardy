package de.ciupka.jeopardy.game.questions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ciupka.jeopardy.game.Category;
import de.ciupka.jeopardy.game.Player;
import de.ciupka.jeopardy.game.questions.answer.Answer;
import de.ciupka.jeopardy.game.questions.answer.SortOption;
import de.ciupka.jeopardy.game.questions.answer.SortOptions;
import de.ciupka.jeopardy.game.questions.reveal.GroupType;
import de.ciupka.jeopardy.game.questions.reveal.Step;
import de.ciupka.jeopardy.game.questions.reveal.StepType;

public class SortQuestion extends AbstractQuestion<SortOptions> implements Evaluatable<SortOption[]> {

    private String[] options;

    public SortQuestion(Category category, String question, int points, SortOptions answer) {
        super(category, question, points, answer, Type.SORT);

        List<String> optionList = Arrays.stream(answer.getOptions())
                .map(SortOption::getName)
                .collect(Collectors.toList());

        Collections.shuffle(optionList);
        this.options = optionList.toArray(String[]::new);

        getGroups().get(GroupType.ANSWER)
                .addStep(new Step(
                        StepType.TEXT,
                        answer.toString()));
    }

    @Override
    protected Answer<SortOptions> parseAnswer(JsonNode node, Player player) {
        ObjectMapper mapper = new ObjectMapper();
        return new Answer<>(player, mapper.convertValue(node, SortOptions.class));
    }

    @Override
    public void evaluateAnswers() {
        int maxCorrect = -1;
        List<Answer<SortOptions>> best = new ArrayList<>();
        SortOption[] correctOrder = getAnswer().getOptions();

        for (Answer<SortOptions> answer : getAnswers()) {
            SortOption[] playerOrder = answer.getAnswer().getOptions();
            int corrects = 0;

            for (int i = 0; i < playerOrder.length; i++) {
                if (playerOrder[i].getName().equals(correctOrder[i].getName())) {
                    corrects++;
                }
            }

            if (corrects < maxCorrect) {
                answer.setCorrect(this, false);
                continue;
            }

            if (corrects > maxCorrect) {
                maxCorrect = corrects;
                for (Answer<SortOptions> a : best) {
                    a.setCorrect(this, false);
                }
                best.clear();
            }

            best.add(answer);
        }

        for (Answer<SortOptions> a : best) {
            a.setCorrect(this, true);
        }
    }

    public String[] getOptions() {
        return options;
    }
}
