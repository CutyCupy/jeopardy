package de.ciupka.jeopardy.game.questions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ciupka.jeopardy.controller.messages.AnswerUpdate;
import de.ciupka.jeopardy.game.Player;

public class SortQuestion extends AbstractQuestion<SortOption[]>
        implements Evaluatable<SortOption[]> {

    private String[] options;

    public SortQuestion(String question, int points, SortOption[] answer) {
        super(question, points, answer, Type.SORT);

        List<String> options = new ArrayList<>(List.of(answer).stream().map((t) -> t.getName()).toList());
        Collections.shuffle(options);

        this.options = options.toArray(new String[options.size()]);
    }

    @Override
    public boolean allowWrongAnswer() {
        return true;
    }

    @Override
    protected Answer<SortOption[]> parseAnswer(JsonNode node, Player player) {
        ObjectMapper mapper = new ObjectMapper();
        return new Answer<SortOption[]>(player, mapper.convertValue(node, SortOption[].class));
    }

    @Override
    public List<AnswerUpdate> getAnswerUpdates() {
        return getAnswers().stream().map((a) -> {
            switch (a.getUpdateType()) {
                case FULL_ANSWER:
                    return new AnswerUpdate(a.getPlayer().getName(),
                            Arrays.stream(a.getAnswer()).map(SortOption::toString).collect(Collectors.joining(", ")));
                case SHORT_ANSWER:
                    return new AnswerUpdate(a.getPlayer().getName(),
                            Arrays.stream(a.getAnswer()).map(SortOption::getName).collect(Collectors.joining(", ")));
                default:
                    return new AnswerUpdate(a.getPlayer().getName(), null);
            }
        }).toList();
    }

    @Override
    public void evaluateAnswers() {
        int max = -1;
        List<Answer<SortOption[]>> right = new ArrayList<>();

        SortOption[] correctAnswer = getAnswer();

        for (Answer<SortOption[]> answer : getAnswers()) {
            int corrects = 0;
            SortOption[] ans = answer.getAnswer();
            for (int i = 0; i < ans.length; i++) {
                if (ans[i].getName().equals(correctAnswer[i].getName())) {
                    corrects++;
                }
            }
            if (corrects < max) {
                answer.setCorrect(this, false);
                continue;
            }
            if (corrects > max) {
                max = corrects;
                for (Answer<SortOption[]> a : right) {
                    a.setCorrect(this, false);
                }

                right.clear();
            }

            right.add(answer);
        }

        for (Answer<SortOption[]> a : right) {
            a.setCorrect(this, true);
        }

    }

    public String[] getOptions() {
        return options;
    }

}
