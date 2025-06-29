package de.ciupka.jeopardy.game.questions.answer;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SortOptions implements Stringable {

    private SortOption[] options;

    public SortOptions(SortOption[] options) {
        this.options = options;
    }

    @Override
    public String toString() {
        return Arrays.stream(options).map(SortOption::toString).collect(Collectors.joining(", "));
    }

    @Override
    public String asShortString() {
        return Arrays.stream(options).map(SortOption::getName).collect(Collectors.joining(", "));
    }

}
