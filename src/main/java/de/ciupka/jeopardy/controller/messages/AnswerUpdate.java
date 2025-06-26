package de.ciupka.jeopardy.controller.messages;

public class AnswerUpdate {

    private String player;
    private String answer;

    public AnswerUpdate(String p, String a) {
        this.player = p;
        this.answer = a;
    }

    public String getPlayer() {
        return player;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
