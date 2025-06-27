package de.ciupka.jeopardy.exception;

public class QuestionNotFoundException extends Exception {
    

    @Override
    public String getMessage() {
        return "Die Frage wurde nicht gefunden!";
    }
}
