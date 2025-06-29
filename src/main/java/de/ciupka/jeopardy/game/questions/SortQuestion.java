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
import de.ciupka.jeopardy.game.questions.reveal.GroupType;
import de.ciupka.jeopardy.game.questions.reveal.Step;
import de.ciupka.jeopardy.game.questions.reveal.StepType;

public class SortQuestion extends AbstractQuestion<SortOption[]> implements Evaluatable<SortOption[]> {

    private String[] options;

    public SortQuestion(Category category, String question, int points, SortOption[] answer) {
        super(category, question, points, answer, Type.SORT);

        List<String> optionList = Arrays.stream(answer)
                .map(SortOption::getName)
                .collect(Collectors.toList());

        Collections.shuffle(optionList);
        this.options = optionList.toArray(String[]::new);

        getGroups().get(GroupType.ANSWER)
                .addStep(new Step(
                        StepType.TEXT,
                        Arrays.stream(answer)
                                .map(SortOption::toString)
                                .collect(Collectors.joining(", "))));
    }

    @Override
    protected Answer<SortOption[]> parseAnswer(JsonNode node, Player player) {
        ObjectMapper mapper = new ObjectMapper();
        return new Answer<>(player, mapper.convertValue(node, SortOption[].class));
    }

    @Override
    public void evaluateAnswers() {
        int maxCorrect = -1;
        List<Answer<SortOption[]>> best = new ArrayList<>();
        SortOption[] correctOrder = getAnswer();

        for (Answer<SortOption[]> answer : getAnswers()) {
            SortOption[] playerOrder = answer.getAnswer();
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
                for (Answer<SortOption[]> a : best) {
                    a.setCorrect(this, false);
                }
                best.clear();
            }

            best.add(answer);
        }

        for (Answer<SortOption[]> a : best) {
            a.setCorrect(this, true);
        }
    }

    public String[] getOptions() {
        return options;
    }
}
