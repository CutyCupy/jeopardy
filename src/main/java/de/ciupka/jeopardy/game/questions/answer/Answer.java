package de.ciupka.jeopardy.game.questions.answer;

import de.ciupka.jeopardy.game.Player;
import de.ciupka.jeopardy.game.questions.AbstractQuestion;

public class Answer<T> {

    private Player player;
    private T answer;

    private Boolean correct;
    private boolean revealed;

    public Answer(Player p, T a) {
        this.player = p;
        this.answer = a;
    }

    public Player getPlayer() {
        return player;
    }

    public T getAnswer() {
        return answer;
    }

    public void setAnswer(T answer) {
        this.answer = answer;
    }

    public void setCorrect(AbstractQuestion<?> question, boolean correct) {
        this.player.updateScore(correct ? question.getPoints() : question.getWrongPoints());
        this.correct = correct;
    }

    public Boolean getCorrect() {
        return this.correct;
    }

    public boolean isRevealed() {
        return this.revealed;
    }

    public void setRevealed(boolean reveal) {
        this.revealed = reveal;
    }

    public String asAnswerText(boolean master) {
        String value = answer instanceof Stringable sAns ? sAns.asShortString() : answer.toString();
        return master || revealed ? value : null;
    }

}
