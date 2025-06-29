package de.ciupka.jeopardy.game.questions.answer;

import java.text.DecimalFormat;

public class SortOption {

    private String name;
    private double value;

    public SortOption() {

    }

    public SortOption(String name, double value) {
        this.name = name;
        this.value = value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("0.####");
        return String.format("%s (%s)", name, df.format(value));
    }

}
