package de.ciupka.jeopardy.controller.messages;

public class AnswerEvaluation {

    private String playerName;
    private boolean isCorrect;

    public AnswerEvaluation(String name, boolean isCorrect) {
        this.playerName = name;
        this.isCorrect = isCorrect;
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

}

