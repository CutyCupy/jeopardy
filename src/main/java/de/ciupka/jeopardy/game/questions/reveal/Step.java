package de.ciupka.jeopardy.game.questions.reveal;

public class Step {

    private StepType type;
    private Object content;
    private boolean revealed;

    public Step(StepType type, Object content) {
        this.type = type;
        this.content = content;
    }

    public StepType getType() {
        return type;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }

}
