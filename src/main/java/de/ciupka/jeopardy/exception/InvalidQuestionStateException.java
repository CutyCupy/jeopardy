package de.ciupka.jeopardy.exception;

public class InvalidQuestionStateException extends Exception {
    @Override
    public String getMessage() {
        return "Die aktuelle Frage hat den Status noch nicht den richtigen Status!";
    }

}
