package de.ciupka.jeopardy.exception;

public class CategoryNotFoundException extends Exception {

    @Override
    public String getMessage() {
        return "Die Kategorie wurde nicht gefunden!";
    }
    
}
