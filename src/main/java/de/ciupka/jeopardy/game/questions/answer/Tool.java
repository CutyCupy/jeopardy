package de.ciupka.jeopardy.game.questions.answer;

public enum Tool {
    BUZZER("Buzzer"),
    TEXT("Text"),
    NUMBER("Zahl"),
    SORT("Sortieren");

    private Tool(String name) {
        this.title = name;
    }

    private String title;

    public String getTitle() {
        return this.title;
    }

}
