package de.ciupka.jeopardy.controller.messages;

import de.ciupka.jeopardy.game.Player;
import de.ciupka.jeopardy.game.questions.AbstractQuestion;
import de.ciupka.jeopardy.game.questions.Evaluatable;
import de.ciupka.jeopardy.game.questions.Type;
import de.ciupka.jeopardy.game.questions.answer.Answer;

public class AnswerUpdate {

    private Player player;
    private String answer;

    private Boolean correct;

    private boolean evaluationEnabled;
    private boolean revealed;

    public AnswerUpdate(AbstractQuestion<?> question, Answer<?> answer, boolean master) {
        this.player = answer.getPlayer();
        this.correct = answer.getCorrect();
        this.answer = answer.asAnswerText(master);

        this.evaluationEnabled = master && !(question instanceof Evaluatable)
                && (question.isLocked() || question.getType().equals(Type.NORMAL)) && correct == null;
        this.revealed = answer.isRevealed();
    }

    public Player getPlayer() {
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

    public boolean isEvaluationEnabled() {
        return evaluationEnabled;
    }

    public boolean isRevealed() {
        return revealed;
    }

}
