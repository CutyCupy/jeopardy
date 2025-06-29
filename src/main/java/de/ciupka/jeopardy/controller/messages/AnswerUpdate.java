package de.ciupka.jeopardy.controller.messages;

import de.ciupka.jeopardy.game.questions.answer.Answer;

public class AnswerUpdate {

    private String player;
    private String answer;
    private Boolean correct;

    public AnswerUpdate(Answer<?> answer, boolean master) {
        this.player = answer.getPlayer().getName();
        this.correct = answer.getCorrect();
        this.answer = answer.asAnswerText(master);
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

    public Boolean getCorrect() {
        return correct;
    }

}
