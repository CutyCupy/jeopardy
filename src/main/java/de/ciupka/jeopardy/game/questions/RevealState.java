package de.ciupka.jeopardy.game.questions;

public enum RevealState {
    HIDDEN, SHOW_CATEGORY, SHOW_QUESTION, SHOW_QUESTION_DATA, LOCK_QUESTION, SHOW_ANSWER;

    public RevealState next() {
        RevealState[] vals = values();
        int nextOrdinal = this.ordinal() + 1;
        return nextOrdinal < vals.length ? vals[nextOrdinal] : this;
    }
    
    public RevealState previous() {
        RevealState[] vals = values();
        int nextOrdinal = this.ordinal() - 1;
        return nextOrdinal < vals.length ? vals[nextOrdinal] : this;
    }
}
