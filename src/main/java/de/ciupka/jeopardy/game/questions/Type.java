package de.ciupka.jeopardy.game.questions;

public enum Type {
    NORMAL("Frage"),
    ESTIMATE("Schätzfrage"),
    SORT("Sortierfrage"),
    VIDEO("Videofrage"),
    HINT("'Wer bin ich?' Frage");

    private Type(String name) {
        this.title = name;
    }

    private String title;

    public String getTitle() {
        return this.title;
    }

}
