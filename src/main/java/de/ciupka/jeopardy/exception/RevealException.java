package de.ciupka.jeopardy.exception;

public class RevealException extends Exception {
    
    private String details;

    public RevealException(String details) {
        this.details = details;
    }

    @Override
    public String getMessage() {
        return String.format("Dieser Revealschritt ist aktuell nicht möglich: %s", details);
    }
}
