package de.ciupka.jeopardy.exception;

import de.ciupka.jeopardy.game.questions.QuestionState;

public class InvalidQuestionStateException extends Exception {

    private QuestionState has;
    private QuestionState needs;

    public InvalidQuestionStateException(QuestionState has, QuestionState needs) {
        this.has = has;
        this.needs = needs;
    }

    @Override
    public String getMessage() {
        return String.format(
                "Die aktuelle Frage hat den Status '%s', benötigt für die Aktion allerdings den Status '%s'!",
                this.has.name(), this.needs.name());
    }

}
