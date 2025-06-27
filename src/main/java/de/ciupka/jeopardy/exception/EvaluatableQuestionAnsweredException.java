package de.ciupka.jeopardy.exception;

import de.ciupka.jeopardy.game.questions.Type;

public class EvaluatableQuestionAnsweredException extends Exception {

    private Type type;

    public EvaluatableQuestionAnsweredException(Type type) {
        this.type = type;
    }

    @Override
    public String getMessage() {
        return String.format("Eine Frage vom Typ '%s' muss nicht durch den Spielleiter bewertet werden!",
                this.type.getTitle());
    }

}
