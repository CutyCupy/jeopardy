package de.ciupka.jeopardy.game.questions;

public enum QuestionState {
    HIDDEN, SHOW_CATEGORY, SHOW_QUESTION, SHOW_QUESTION_DATA, LOCK_QUESTION, SHOW_ANSWER;

    public QuestionState next() {
        QuestionState[] vals = values();
        int nextOrdinal = this.ordinal() + 1;
        return nextOrdinal < vals.length ? vals[nextOrdinal] : this;
    }
    
    public QuestionState previous() {
        QuestionState[] vals = values();
        int nextOrdinal = this.ordinal() - 1;
        return nextOrdinal >= 0 ? vals[nextOrdinal] : this;
    }
}
