package de.ciupka.jeopardy.controller.messages;

public class QuestionIdentifier {
    private int category;
    private int question;

    public QuestionIdentifier(int category, int question) {
        this.category = category;
        this.question = question;
    }

    public int getCategory() {
        return category;
    }

    public int getQuestion() {
        return question;
    }
}
