package de.ciupka.jeopardy.exception;

public class QuestionAlreadySelectedException extends Exception {

    @Override
    public String getMessage() {
        return "Es ist bereits eine Frage ausgewählt!";
    }

    
}
