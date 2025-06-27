package de.ciupka.jeopardy.exception;

public class NoQuestionSelectedException extends Exception {

    @Override
    public String getMessage() {
        return "Aktuell ist keine Frage ausgewählt!";
    }
    
}
