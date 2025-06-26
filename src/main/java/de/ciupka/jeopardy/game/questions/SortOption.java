package de.ciupka.jeopardy.game.questions;

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


}
